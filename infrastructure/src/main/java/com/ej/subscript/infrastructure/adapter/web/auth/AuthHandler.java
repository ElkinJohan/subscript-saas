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
 * Maneja el endpoint de autenticación.
 *
 * <h3>Flujo de login</h3>
 * <ol>
 *   <li>Parsea y valida el {@link LoginRequest}</li>
 *   <li>Busca el Owner por email vía {@link OwnerUseCase#findByEmail}</li>
 *   <li>Verifica la contraseña con BCrypt — si no coincide emite 401</li>
 *   <li>Genera access token (15 min) + refresh token (7 días) y los devuelve</li>
 * </ol>
 *
 * <p>El 401 es idéntico para "email no existe" y "contraseña incorrecta" a propósito:
 * un mensaje diferenciado permitiría enumerar usuarios válidos.
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
                                "Credenciales inválidas", 401, "Email o contraseña incorrectos")))
                )
                .flatMap(owner -> auditAndIssue(owner, AuditEventType.AUTH_LOGIN_SUCCESS))
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * Renueva el par de tokens a partir de un refresh token válido y rota el refresh entrante.
     * <p>
     * Tras validar el token, su {@code jti} se agrega a la blacklist con TTL igual al
     * tiempo restante de vida — cualquier intento posterior de reusarlo es rechazado
     * por {@link com.ej.subscript.infrastructure.security.BlacklistAwareJwtDecoder} y
     * registrado como {@link AuditEventType#AUTH_TOKEN_REUSE_DETECTED}, señal de robo
     * o cliente mal implementado. La blacklist ocurre <b>antes</b> de emitir el par
     * nuevo: si la emisión fallara, el refresh viejo queda igualmente revocado.
     * <p>
     * Cualquier otro fallo (firma inválida, expirado, claim faltante, owner borrado)
     * devuelve 401 genérico para no filtrar información al atacante.
     */
    public Mono<ServerResponse> refresh(ServerRequest request) {
        return request.bodyToMono(RefreshRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> jwtDecoder.decode(req.refreshToken()))
                .onErrorResume(RevokedTokenException.class, this::auditReuseAndReject)
                .onErrorMap(JwtException.class, e -> new BusinessException(
                        "Token inválido", 401, "Refresh token inválido o expirado"))
                .flatMap(jwt -> {
                    if (!REFRESH_VALUE.equals(jwt.getClaimAsString(REFRESH_CLAIM))) {
                        return Mono.error(new BusinessException(
                                "Token inválido", 401, "El token recibido no es un refresh token"));
                    }
                    return ownerUseCase.findById(jwt.getSubject())
                            .onErrorMap(BusinessException.class, e -> new BusinessException(
                                    "Token inválido", 401, "Refresh token inválido o expirado"))
                            .flatMap(owner -> rotateAndIssue(jwt, owner));
                })
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * Cierra la sesión revocando access token (del header) y refresh token (del body).
     * <p>
     * Cada token se persiste en la blacklist con TTL igual a su tiempo restante de vida,
     * de modo que Redis libera la memoria automáticamente al expirar el JWT original.
     * El access token sale del {@link JwtAuthenticationToken} ya validado por el filtro
     * de Spring Security, por eso este endpoint requiere autenticación.
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
     * Genera un par de tokens nuevo y registra el evento de auditoría asociado.
     * <p>
     * Envuelto en {@link Mono#defer} para que la generación de los tokens —que
     * captura {@code Instant.now()} y produce nuevos {@code jti}— ocurra en el
     * momento de la suscripción, no al construir el {@code Mono}. Esto evita que
     * operadores upstream que retrasen o aborten la cadena (por ejemplo
     * {@link #rotateAndIssue}, que blacklistea el refresh viejo primero) emitan
     * tokens prematuros que después no se usen.
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
     * Rota el refresh entrante y emite el par nuevo.
     * <p>
     * El orden es deliberado y forma parte del contrato de seguridad:
     * <b>blacklistear primero, emitir después</b>. Si la emisión del par nuevo
     * fallara —por un error transitorio del JwtEncoder, por ejemplo— el refresh
     * viejo queda igualmente revocado, así que un retry del cliente será
     * detectado como reuso y no como una nueva renovación válida.
     *
     * @param oldRefresh JWT del refresh entrante, ya validado y no expirado.
     * @param owner      Owner referenciado en el {@code sub} del refresh.
     * @return par de tokens nuevo más el evento {@code AUTH_TOKEN_REFRESHED} ya persistido.
     */
    private Mono<TokenResponse> rotateAndIssue(Jwt oldRefresh, Owner owner) {
        return tokenBlacklist.blacklist(oldRefresh.getId(), remaining(oldRefresh.getExpiresAt()))
                .then(auditAndIssue(owner, AuditEventType.AUTH_TOKEN_REFRESHED));
    }

    /**
     * Maneja el reuso de un refresh token previamente rotado.
     * <p>
     * Emite un evento {@link AuditEventType#AUTH_TOKEN_REUSE_DETECTED} con el
     * {@code jti} y el {@code subject} del token rechazado, y luego propaga un
     * 401 genérico —idéntico al de cualquier otro fallo de validación de
     * refresh— para no filtrar al cliente la causa real del rechazo.
     * El audit ocurre <b>antes</b> de la respuesta, garantizando trazabilidad
     * incluso si el cliente reintenta inmediatamente.
     */
    private Mono<Jwt> auditReuseAndReject(RevokedTokenException ex) {
        AuditEvent event = AuditEvent.of(
                AuditEventType.AUTH_TOKEN_REUSE_DETECTED,
                parseOwnerId(ex.getSubject()),
                Map.of("jti", ex.getJti() == null ? "unknown" : ex.getJti()));
        return auditLog.record(event)
                .then(Mono.error(new BusinessException(
                        "Token inválido", 401, "Refresh token inválido o expirado")));
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
        return Mono.error(new BusinessException("Datos inválidos", 400, detail));
    }
}
