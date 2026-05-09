package com.ej.subscript.infrastructure.adapter.web.owner;

import com.ej.subscript.application.usecase.OwnerUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maneja los endpoints REST del agregado Owner.
 *
 * <p>El handler se mantiene fino a propósito: la validación estructural la
 * resuelve Bean Validation sobre el {@link OwnerRequest}, las invariantes de
 * dominio las hace el compact constructor de {@link Owner}, y la lógica de
 * unicidad y persistencia vive en {@link OwnerUseCase}. Esto deja al handler
 * solo dos responsabilidades: traducción HTTP↔dominio y hashing de password.
 *
 * <p>El hashing BCrypt ocurre antes de instanciar el {@link Owner} para que
 * la contraseña en texto plano no cruce la frontera del modelo de dominio.
 */
@Component
@RequiredArgsConstructor
public class OwnerHandler {

    private final OwnerUseCase ownerUseCase;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registra un Owner nuevo. Endpoint público — es el punto de entrada de la
     * plataforma.
     *
     * <p>Pipeline reactivo: valida el body → hashea password → construye
     * {@link Owner} (que aplica sus propias invariantes) → delega al
     * {@link OwnerUseCase#register use case} (que verifica unicidad de email
     * y persiste) → mapea a {@link OwnerResponse} sin exponer el hash.
     *
     * @return {@code 201 Created} con el Owner persistido (sin password).
     *         Errores: {@code 400} validación de schema, {@code 409} email
     *         duplicado, {@code 422} invariantes de dominio.
     */
    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(OwnerRequest.class)
                .flatMap(this::validate)
                .map(req -> Owner.create(
                        req.nit(), req.name(), req.email(), req.phone(),
                        req.businessName(), req.gracePeriodDays(),
                        passwordEncoder.encode(req.password())))
                .flatMap(ownerUseCase::register)
                .map(OwnerResponse::from)
                .flatMap(body -> ServerResponse.status(HttpStatus.CREATED).bodyValue(body));
    }

    /**
     * Devuelve el perfil del Owner referenciado por el path. Requiere autenticación.
     *
     * <p>El filtro de Spring Security valida el access token antes de que este
     * método se ejecute, por eso un GET sin token siempre retorna {@code 401}
     * y no filtra si el {@code id} existe.
     *
     * @return {@code 200 OK} con {@link OwnerResponse}; {@code 404} si el
     *         Owner no existe; {@code 401} si el token es inválido o revocado.
     */
    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("id");
        return ownerUseCase.findById(id)
                .map(OwnerResponse::from)
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
