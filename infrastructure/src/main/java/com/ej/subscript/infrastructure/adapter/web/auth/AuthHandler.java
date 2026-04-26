package com.ej.subscript.infrastructure.adapter.web.auth;

import com.ej.subscript.application.usecase.OwnerUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.infrastructure.security.JwtService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
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

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> ownerUseCase.findByEmail(req.email())
                        .filter(owner -> passwordEncoder.matches(req.password(), owner.passwordHash()))
                        .switchIfEmpty(Mono.error(new BusinessException(
                                "Credenciales inválidas", 401, "Email o contraseña incorrectos")))
                )
                .map(owner -> new TokenResponse(
                        jwtService.generateAccessToken(owner),
                        jwtService.generateRefreshToken(owner)
                ))
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    /**
     * Renueva el par de tokens a partir de un refresh token válido.
     * <p>
     * El refresh token debe llevar el claim {@code type=refresh} — un access token
     * NO sirve para refrescar (defensa en profundidad contra abuso de scope).
     * Cualquier fallo (firma inválida, expirado, claim faltante, owner borrado)
     * devuelve 401 genérico para no filtrar información al atacante.
     */
    public Mono<ServerResponse> refresh(ServerRequest request) {
        return request.bodyToMono(RefreshRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> jwtDecoder.decode(req.refreshToken()))
                .onErrorMap(JwtException.class, e -> new BusinessException(
                        "Token inválido", 401, "Refresh token inválido o expirado"))
                .flatMap(jwt -> {
                    if (!REFRESH_VALUE.equals(jwt.getClaimAsString(REFRESH_CLAIM))) {
                        return Mono.error(new BusinessException(
                                "Token inválido", 401, "El token recibido no es un refresh token"));
                    }
                    return ownerUseCase.findById(jwt.getSubject())
                            .onErrorMap(BusinessException.class, e -> new BusinessException(
                                    "Token inválido", 401, "Refresh token inválido o expirado"));
                })
                .map(owner -> new TokenResponse(
                        jwtService.generateAccessToken(owner),
                        jwtService.generateRefreshToken(owner)
                ))
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
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
