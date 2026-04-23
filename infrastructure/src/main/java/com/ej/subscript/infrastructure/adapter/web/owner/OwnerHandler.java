package com.ej.subscript.infrastructure.adapter.web.owner;

import com.ej.subscript.application.usecase.OwnerUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OwnerHandler {

    private final OwnerUseCase ownerUseCase;
    private final Validator validator;

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(OwnerRequest.class)
                .flatMap(this::validate)
                .map(req -> Owner.create(req.nit(), req.name(), req.email(),
                        req.phone(), req.businessName(), req.gracePeriodDays()))
                .flatMap(ownerUseCase::register)
                .map(OwnerResponse::from)
                .flatMap(body -> ServerResponse.status(HttpStatus.CREATED).bodyValue(body));
    }

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
