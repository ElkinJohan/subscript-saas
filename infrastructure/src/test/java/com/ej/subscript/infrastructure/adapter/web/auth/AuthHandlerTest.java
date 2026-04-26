package com.ej.subscript.infrastructure.adapter.web.auth;

import com.ej.subscript.application.usecase.OwnerUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.infrastructure.security.JwtService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.web.reactive.server.HttpHandlerConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AuthHandlerTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final Owner OWNER = new Owner(
            OWNER_ID, "900123", "Juan", "juan@gym.com", "300", "GymFit", 3,
            "$2a$10$hashedPasswordForTests"
    );

    private WebTestClient client;
    private OwnerUseCase ownerUseCase;
    private JwtService jwtService;
    private PasswordEncoder passwordEncoder;
    private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        ownerUseCase = Mockito.mock(OwnerUseCase.class);
        jwtService = Mockito.mock(JwtService.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        jwtDecoder = Mockito.mock(ReactiveJwtDecoder.class);

        var handler = new AuthHandler(
                ownerUseCase,
                jwtService,
                passwordEncoder,
                Validation.buildDefaultValidatorFactory().getValidator(),
                jwtDecoder
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
                Mono.error(new BusinessException("Credenciales inválidas", 401, "Email o contraseña incorrectos"))
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
                Mono.error(new BusinessException("Owner no encontrado", 404, "No existe"))
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

    private static Jwt jwt(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Jwt.Builder builder = Jwt.withTokenValue("token-value")
                .header("alg", "RS256")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(60))
                .subject(subject);
        extraClaims.forEach(builder::claim);
        return builder.build();
    }
}
