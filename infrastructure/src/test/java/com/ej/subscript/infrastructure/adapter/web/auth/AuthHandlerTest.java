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
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.reactive.server.HttpHandlerConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthHandlerTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final Owner OWNER = new Owner(
            OWNER_ID, "900123", "Juan", "juan@gym.com", "300", "GymFit", 3,
            "$2a$10$hashedPasswordForTests"
    );

    private WebTestClient client;
    private AuthHandler handler;
    private OwnerUseCase ownerUseCase;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private ReactiveJwtDecoder jwtDecoder;
    private TokenBlacklist tokenBlacklist;
    private AuditLog auditLog;

    @BeforeEach
    void setUp() {
        ownerUseCase = Mockito.mock(OwnerUseCase.class);
        jwtService = Mockito.mock(JwtService.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtDecoder = Mockito.mock(ReactiveJwtDecoder.class);
        tokenBlacklist = Mockito.mock(TokenBlacklist.class);
        auditLog = Mockito.mock(AuditLog.class);
        when(auditLog.record(any(AuditEvent.class))).thenReturn(Mono.empty());
        when(tokenBlacklist.blacklist(anyString(), any(Duration.class))).thenReturn(Mono.empty());

        handler = new AuthHandler(
                ownerUseCase,
                jwtService,
                passwordEncoder,
                Validation.buildDefaultValidatorFactory().getValidator(),
                jwtDecoder,
                tokenBlacklist,
                auditLog
        );
        WebExceptionHandler exHandler = (exchange, ex) -> {
            if (ex instanceof BusinessException be)
                exchange.getResponse().setStatusCode(HttpStatus.valueOf(be.status()));
            else
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        };
        HttpHandler httpHandler = WebHttpHandlerBuilder
                .webHandler(RouterFunctions.toWebHandler(new AuthRouter().authRoutes(handler)))
                .exceptionHandler(exHandler)
                .build();
        client = WebTestClient.bindToServer(new HttpHandlerConnector(httpHandler)).build();
    }

    // --- Login -----------------------------------------------------------

    @Test
    void shouldLoginAndReturnTokens() {
        when(ownerUseCase.findByEmail(OWNER.email())).thenReturn(Mono.just(OWNER));
        when(passwordEncoder.matches("password123", OWNER.passwordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(OWNER)).thenReturn("access-jwt");
        when(jwtService.generateRefreshToken(OWNER)).thenReturn("refresh-jwt");

        client.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(OWNER.email(), "password123"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(body -> {
                    assertThat(body.accessToken()).isEqualTo("access-jwt");
                    assertThat(body.refreshToken()).isEqualTo("refresh-jwt");
                });
    }

    @Test
    void shouldAuditSuccessfulLogin() {
        when(ownerUseCase.findByEmail(OWNER.email())).thenReturn(Mono.just(OWNER));
        when(passwordEncoder.matches("password123", OWNER.passwordHash())).thenReturn(true);
        when(jwtService.generateAccessToken(OWNER)).thenReturn("access-jwt");
        when(jwtService.generateRefreshToken(OWNER)).thenReturn("refresh-jwt");

        client.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(OWNER.email(), "password123"))
                .exchange()
                .expectStatus().isOk();

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditLog).record(captor.capture());
        AuditEvent event = captor.getValue();
        assertThat(event.type()).isEqualTo(AuditEventType.AUTH_LOGIN_SUCCESS);
        assertThat(event.ownerId()).isEqualTo(OWNER_ID);
        assertThat(event.data()).containsEntry("email", OWNER.email());
    }

    @Test
    void shouldReturn401WhenPasswordDoesNotMatch() {
        when(ownerUseCase.findByEmail(OWNER.email())).thenReturn(Mono.just(OWNER));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        client.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest(OWNER.email(), "wrong"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn401WhenEmailDoesNotExist() {
        when(ownerUseCase.findByEmail(anyString())).thenReturn(
                Mono.error(new BusinessException("Invalid credentials", 401, "Email or password are incorrect"))
        );

        client.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest("ghost@gym.com", "password123"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn400WhenLoginEmailIsInvalid() {
        client.post().uri("/api/auth/login")
                .bodyValue(new LoginRequest("not-an-email", "password123"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    // --- Refresh ---------------------------------------------------------

    @Test
    void shouldRefreshAndReturnNewTokens() {
        Jwt refreshJwt = jwt(OWNER_ID.toString(), Map.of("type", "refresh"));
        when(jwtDecoder.decode("good-refresh")).thenReturn(Mono.just(refreshJwt));
        when(ownerUseCase.findById(OWNER_ID.toString())).thenReturn(Mono.just(OWNER));
        when(jwtService.generateAccessToken(OWNER)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(OWNER)).thenReturn("new-refresh");

        client.post().uri("/api/auth/refresh")
                .bodyValue(new RefreshRequest("good-refresh"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TokenResponse.class)
                .value(body -> {
                    assertThat(body.accessToken()).isEqualTo("new-access");
                    assertThat(body.refreshToken()).isEqualTo("new-refresh");
                });
    }

    @Test
    void shouldReturn401WhenRefreshTokenIsInvalid() {
        when(jwtDecoder.decode(anyString())).thenReturn(
                Mono.error(new BadJwtException("malformed"))
        );

        client.post().uri("/api/auth/refresh")
                .bodyValue(new RefreshRequest("garbage"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn401WhenAccessTokenIsUsedForRefresh() {
        Jwt accessJwt = jwt(OWNER_ID.toString(), Map.of("email", OWNER.email()));
        when(jwtDecoder.decode("access-as-refresh")).thenReturn(Mono.just(accessJwt));

        client.post().uri("/api/auth/refresh")
                .bodyValue(new RefreshRequest("access-as-refresh"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn401WhenOwnerNoLongerExists() {
        Jwt refreshJwt = jwt(OWNER_ID.toString(), Map.of("type", "refresh"));
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(refreshJwt));
        when(ownerUseCase.findById(anyString())).thenReturn(
                Mono.error(new BusinessException("Owner not found", 404, "Not found"))
        );

        client.post().uri("/api/auth/refresh")
                .bodyValue(new RefreshRequest("valid-refresh"))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldReturn400WhenRefreshTokenIsBlank() {
        client.post().uri("/api/auth/refresh")
                .bodyValue(new RefreshRequest(""))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldBlacklistOldRefreshTokenBeforeIssuingNewOnes() {
        String oldJti = UUID.randomUUID().toString();
        Jwt refreshJwt = jwt(oldJti, OWNER_ID.toString(), Map.of("type", "refresh"));
        when(jwtDecoder.decode("good-refresh")).thenReturn(Mono.just(refreshJwt));
        when(ownerUseCase.findById(OWNER_ID.toString())).thenReturn(Mono.just(OWNER));
        when(jwtService.generateAccessToken(OWNER)).thenReturn("new-access");
        when(jwtService.generateRefreshToken(OWNER)).thenReturn("new-refresh");

        client.post().uri("/api/auth/refresh")
                .bodyValue(new RefreshRequest("good-refresh"))
                .exchange()
                .expectStatus().isOk();

        verify(tokenBlacklist).blacklist(eq(oldJti), any(Duration.class));
    }

    @Test
    void shouldReturn401AndAuditReuseWhenRefreshTokenIsRevoked() {
        String revokedJti = UUID.randomUUID().toString();
        when(jwtDecoder.decode(anyString())).thenReturn(
                Mono.error(new RevokedTokenException(revokedJti, OWNER_ID.toString()))
        );

        client.post().uri("/api/auth/refresh")
                .bodyValue(new RefreshRequest("revoked-refresh"))
                .exchange()
                .expectStatus().isUnauthorized();

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditLog).record(captor.capture());
        AuditEvent event = captor.getValue();
        assertThat(event.type()).isEqualTo(AuditEventType.AUTH_TOKEN_REUSE_DETECTED);
        assertThat(event.ownerId()).isEqualTo(OWNER_ID);
        assertThat(event.data()).containsEntry("jti", revokedJti);
        verify(jwtService, never()).generateAccessToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    @Test
    void shouldNotIssueNewTokensIfBlacklistFails() {
        Jwt refreshJwt = jwt(OWNER_ID.toString(), Map.of("type", "refresh"));
        when(jwtDecoder.decode(anyString())).thenReturn(Mono.just(refreshJwt));
        when(ownerUseCase.findById(OWNER_ID.toString())).thenReturn(Mono.just(OWNER));
        when(tokenBlacklist.blacklist(anyString(), any(Duration.class)))
                .thenReturn(Mono.error(new RuntimeException("Redis caído")));

        client.post().uri("/api/auth/refresh")
                .bodyValue(new RefreshRequest("good-refresh"))
                .exchange()
                .expectStatus().is5xxServerError();

        verify(jwtService, never()).generateAccessToken(any());
        verify(jwtService, never()).generateRefreshToken(any());
    }

    // --- Logout ----------------------------------------------------------

    @Test
    void shouldBlacklistBothTokensOnLogout() {
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();
        Jwt accessJwt = jwt(accessJti, OWNER_ID.toString(), Map.of("email", OWNER.email()));
        Jwt refreshJwt = jwt(refreshJti, OWNER_ID.toString(), Map.of("type", "refresh"));

        ServerRequest request = Mockito.mock(ServerRequest.class);
        when(request.bodyToMono(RefreshRequest.class)).thenReturn(Mono.just(new RefreshRequest("refresh-jwt")));
        doReturn(Mono.just(new JwtAuthenticationToken(accessJwt))).when(request).principal();
        when(jwtDecoder.decode("refresh-jwt")).thenReturn(Mono.just(refreshJwt));
        when(tokenBlacklist.blacklist(anyString(), any(Duration.class))).thenReturn(Mono.empty());

        StepVerifier.create(handler.logout(request))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();

        ArgumentCaptor<String> jtiCaptor = ArgumentCaptor.forClass(String.class);
        verify(tokenBlacklist, Mockito.times(2)).blacklist(jtiCaptor.capture(), any(Duration.class));
        assertThat(jtiCaptor.getAllValues()).containsExactlyInAnyOrder(accessJti, refreshJti);
    }

    @Test
    void shouldStillBlacklistAccessTokenWhenRefreshTokenIsInvalid() {
        String accessJti = UUID.randomUUID().toString();
        Jwt accessJwt = jwt(accessJti, OWNER_ID.toString(), Map.of());

        ServerRequest request = Mockito.mock(ServerRequest.class);
        when(request.bodyToMono(RefreshRequest.class)).thenReturn(Mono.just(new RefreshRequest("garbage")));
        doReturn(Mono.just(new JwtAuthenticationToken(accessJwt))).when(request).principal();
        when(jwtDecoder.decode("garbage")).thenReturn(Mono.error(new BadJwtException("malformed")));
        when(tokenBlacklist.blacklist(anyString(), any(Duration.class))).thenReturn(Mono.empty());

        StepVerifier.create(handler.logout(request))
                .assertNext(response -> assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT))
                .verifyComplete();

        verify(tokenBlacklist).blacklist(eq(accessJti), any(Duration.class));
        verify(tokenBlacklist, never()).blacklist(eq("garbage"), any(Duration.class));
    }

    private static Jwt jwt(String subject, Map<String, Object> extraClaims) {
        return jwt(UUID.randomUUID().toString(), subject, extraClaims);
    }

    private static Jwt jwt(String jti, String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Jwt.Builder builder = Jwt.withTokenValue("token-value")
                .header("alg", "RS256")
                .jti(jti)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .subject(subject);
        extraClaims.forEach(builder::claim);
        return builder.build();
    }
}
