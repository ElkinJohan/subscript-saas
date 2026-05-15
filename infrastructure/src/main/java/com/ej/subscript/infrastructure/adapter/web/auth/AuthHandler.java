package com.ej.subscript.infrastructure.adapter.web.auth;

import com.ej.subscript.application.usecase.OwnerUseCase;
import com.ej.subscript.domain.audit.AuditEvent;
import com.ej.subscript.domain.audit.AuditEventType;
import com.ej.subscript.domain.audit.AuditLog;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.infrastructure.security.JwtService;
import com.ej.subscript.infrastructure.security.RevokedTokenException;
import com.ej.subscript.infrastructure.security.TokenBlacklist;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handles the authentication endpoints.
 *
 * <h3>Login flow</h3>
 * <ol>
 *   <li>Parse and validate the {@link LoginRequest}</li>
 *   <li>Look up the Owner by email via {@link OwnerUseCase#findByEmail}</li>
 *   <li>Verify the password with BCrypt — mismatch yields a 401</li>
 *   <li>Issue an access token (15 min) + refresh token (7 days) and return both</li>
 * </ol>
 *
 * <p>The 401 is intentionally identical for "email does not exist" and
 * "wrong password": a differentiated message would let an attacker
 * enumerate valid users.
 */
@Component
@RequiredArgsConstructor
public class AuthHandler {

    private static final String REFRESH_CLAIM = "type";
    private static final String REFRESH_VALUE = "refresh";

    private final OwnerUseCase ownerUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;
    private final ReactiveJwtDecoder jwtDecoder;
    private final TokenBlacklist tokenBlacklist;
    private final AuditLog auditLog;

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> ownerUseCase.findByEmail(req.email())
                        .filter(owner -> passwordEncoder.matches(req.password(), owner.passwordHash()))
                        .switchIfEmpty(Mono.error(new BusinessException(
                                "Invalid credentials", 401, "Email or password are incorrect")))
                )
                .flatMap(owner -> auditAndIssue(owner, AuditEventType.AUTH_LOGIN_SUCCESS))
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * Renews the token pair from a valid refresh token and rotates the
     * incoming refresh.
     * <p>
     * After the token is validated, its {@code jti} is added to the
     * blacklist with a TTL equal to its remaining lifetime — any later
     * attempt to reuse it is rejected by
     * {@link com.ej.subscript.infrastructure.security.BlacklistAwareJwtDecoder}
     * and recorded as {@link AuditEventType#AUTH_TOKEN_REUSE_DETECTED}, a
     * signal of theft or a misbehaving client. Blacklisting happens
     * <b>before</b> issuing the new pair: if issuance fails, the old
     * refresh stays revoked anyway.
     * <p>
     * Any other failure (invalid signature, expired, missing claim, deleted
     * owner) returns a generic 401 so no information leaks to the attacker.
     */
    public Mono<ServerResponse> refresh(ServerRequest request) {
        return request.bodyToMono(RefreshRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> jwtDecoder.decode(req.refreshToken()))
                .onErrorResume(RevokedTokenException.class, this::auditReuseAndReject)
                .onErrorMap(JwtException.class, e -> new BusinessException(
                        "Invalid token", 401, "Refresh token is invalid or expired"))
                .flatMap(jwt -> {
                    if (!REFRESH_VALUE.equals(jwt.getClaimAsString(REFRESH_CLAIM))) {
                        return Mono.error(new BusinessException(
                                "Invalid token", 401, "The provided token is not a refresh token"));
                    }
                    return ownerUseCase.findById(jwt.getSubject())
                            .onErrorMap(BusinessException.class, e -> new BusinessException(
                                    "Invalid token", 401, "Refresh token is invalid or expired"))
                            .flatMap(owner -> rotateAndIssue(jwt, owner));
                })
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * Closes the session by revoking the access token (from the header) and
     * the refresh token (from the body).
     * <p>
     * Each token is persisted in the blacklist with a TTL equal to its
     * remaining lifetime, so Redis frees the memory automatically once the
     * original JWT expires. The access token comes from the
     * {@link JwtAuthenticationToken} already validated by Spring Security,
     * which is why this endpoint requires authentication.
     */
    public Mono<ServerResponse> logout(ServerRequest request) {
        Mono<RefreshRequest> body = request.bodyToMono(RefreshRequest.class)
                .flatMap(this::validate);
        Mono<Jwt> accessJwt = request.principal()
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken);

        return Mono.zip(accessJwt, body)
                .flatMap(tuple -> blacklistAccess(tuple.getT1())
                        .then(blacklistRefresh(tuple.getT2().refreshToken()))
                        .then(auditLog.record(AuditEvent.of(
                                AuditEventType.AUTH_LOGOUT,
                                ownerIdOf(tuple.getT1()),
                                Map.of()))))
                .then(ServerResponse.noContent().build());
    }

    /**
     * Issues a new token pair and records the associated audit event.
     * <p>
     * Wrapped in {@link Mono#defer} so token generation — which captures
     * {@code Instant.now()} and mints new {@code jti}s — happens at
     * subscription time, not at chain-construction time. This prevents
     * upstream operators that delay or abort the chain (for example
     * {@link #rotateAndIssue}, which blacklists the old refresh first) from
     * minting tokens that go unused.
     */
    private Mono<TokenResponse> auditAndIssue(Owner owner, AuditEventType type) {
        return Mono.defer(() -> {
            TokenResponse tokens = new TokenResponse(
                    jwtService.generateAccessToken(owner),
                    jwtService.generateRefreshToken(owner)
            );
            AuditEvent event = AuditEvent.of(type, owner.id(), Map.of("email", owner.email()));
            return auditLog.record(event).thenReturn(tokens);
        });
    }

    /**
     * Rotates the incoming refresh and issues a new pair.
     * <p>
     * The order is deliberate and part of the security contract:
     * <b>blacklist first, issue afterwards</b>. If issuing the new pair
     * fails — e.g. a transient {@code JwtEncoder} error — the old refresh
     * is still revoked, so a client retry surfaces as a reuse signal
     * instead of a new valid renewal.
     *
     * @param oldRefresh validated, non-expired JWT of the incoming refresh.
     * @param owner      Owner referenced by the refresh's {@code sub}.
     * @return new token pair plus the persisted {@code AUTH_TOKEN_REFRESHED} event.
     */
    private Mono<TokenResponse> rotateAndIssue(Jwt oldRefresh, Owner owner) {
        return tokenBlacklist.blacklist(oldRefresh.getId(), remaining(oldRefresh.getExpiresAt()))
                .then(auditAndIssue(owner, AuditEventType.AUTH_TOKEN_REFRESHED));
    }

    /**
     * Handles reuse of a previously rotated refresh token.
     * <p>
     * Emits an {@link AuditEventType#AUTH_TOKEN_REUSE_DETECTED} event with
     * the rejected token's {@code jti} and {@code subject}, then propagates
     * a generic 401 — identical to every other refresh-validation failure
     * — so the real cause is never leaked to the client. The audit happens
     * <b>before</b> the response, guaranteeing traceability even if the
     * client retries immediately.
     */
    private Mono<Jwt> auditReuseAndReject(RevokedTokenException ex) {
        AuditEvent event = AuditEvent.of(
                AuditEventType.AUTH_TOKEN_REUSE_DETECTED,
                parseOwnerId(ex.getSubject()),
                Map.of("jti", ex.getJti() == null ? "unknown" : ex.getJti()));
        return auditLog.record(event)
                .then(Mono.error(new BusinessException(
                        "Invalid token", 401, "Refresh token is invalid or expired")));
    }

    private static UUID parseOwnerId(String subject) {
        if (subject == null) return null;
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static UUID ownerIdOf(Jwt jwt) {
        return parseOwnerId(jwt.getSubject());
    }

    private Mono<Void> blacklistAccess(Jwt jwt) {
        return tokenBlacklist.blacklist(jwt.getId(), remaining(jwt.getExpiresAt()));
    }

    private Mono<Void> blacklistRefresh(String token) {
        return jwtDecoder.decode(token)
                .onErrorResume(JwtException.class, e -> Mono.empty())
                .flatMap(jwt -> tokenBlacklist.blacklist(jwt.getId(), remaining(jwt.getExpiresAt())));
    }

    private static Duration remaining(Instant expiresAt) {
        if (expiresAt == null) return Duration.ZERO;
        Duration delta = Duration.between(Instant.now(), expiresAt);
        return delta.isNegative() ? Duration.ZERO : delta;
    }

    private <T> Mono<T> validate(T body) {
        Set<ConstraintViolation<T>> violations = validator.validate(body);
        if (violations.isEmpty()) return Mono.just(body);
        String detail = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        return Mono.error(new BusinessException("Invalid input", 400, detail));
    }
}
