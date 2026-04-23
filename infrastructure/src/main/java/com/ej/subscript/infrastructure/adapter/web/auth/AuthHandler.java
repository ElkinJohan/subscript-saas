package com.ej.subscript.infrastructure.adapter.web.auth;

import com.ej.subscript.application.usecase.OwnerUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.infrastructure.security.JwtService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    private final OwnerUseCase ownerUseCase;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

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

    private <T> Mono<T> validate(T body) {
        Set<ConstraintViolation<T>> violations = validator.validate(body);
        if (violations.isEmpty()) return Mono.just(body);
        String detail = violations.stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        return Mono.error(new BusinessException("Datos inválidos", 400, detail));
    }
}
