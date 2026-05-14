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
 * Handles the REST endpoints for the Owner aggregate.
 *
 * <p>The handler is intentionally thin: schema validation lives in Bean
 * Validation on the {@link OwnerRequest}, domain invariants live in
 * {@link Owner}'s compact constructor, and uniqueness + persistence live
 * in {@link OwnerUseCase}. The handler is left with only two
 * responsibilities: HTTP ↔ domain translation and password hashing.
 *
 * <p>BCrypt hashing happens before instantiating the {@link Owner} so the
 * plaintext password never crosses the domain-model boundary.
 */
@Component
@RequiredArgsConstructor
public class OwnerHandler {

    private final OwnerUseCase ownerUseCase;
    private final Validator validator;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new Owner. Public endpoint — entry point to the platform.
     *
     * <p>Reactive pipeline: validate the body → hash the password → build
     * an {@link Owner} (which enforces its own invariants) → delegate to
     * {@link OwnerUseCase#register} (which enforces uniqueness and
     * persists) → map to {@link OwnerResponse} without exposing the hash.
     *
     * @return {@code 201 Created} with the persisted Owner (no password).
     *         Errors: {@code 400} schema validation, {@code 409} duplicate
     *         email or NIT, {@code 422} domain invariants.
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
     * Returns the Owner profile referenced by the path. Requires
     * authentication.
     *
     * <p>The Spring Security filter validates the access token before this
     * method runs, so a GET without a token always returns {@code 401}
     * and never leaks whether the {@code id} exists.
     *
     * @return {@code 200 OK} with {@link OwnerResponse}; {@code 404} when
     *         the Owner does not exist; {@code 401} when the token is
     *         missing, invalid or revoked.
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
        return Mono.error(new BusinessException("Invalid input", 400, detail));
    }
}
