package com.ej.subscript.infrastructure.adapter.web.plan;

import com.ej.subscript.application.usecase.PlanUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Plan;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PlanHandler {

    private final PlanUseCase planUseCase;
    private final Validator validator;

    public PlanHandler(PlanUseCase planUseCase, Validator validator) {
        this.planUseCase = planUseCase;
        this.validator = validator;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        UUID ownerId = UUID.fromString(request.pathVariable("ownerId"));
        return request.bodyToMono(PlanRequest.class)
                .flatMap(this::validate)
                .map(req -> Plan.create(ownerId, req.name(), req.description(),
                        new Money(req.priceAmount(), req.priceCurrency()), req.durationDays()))
                .flatMap(planUseCase::create)
                .map(PlanResponse::from)
                .flatMap(body -> ServerResponse.status(HttpStatus.CREATED).bodyValue(body));
    }

    public Mono<ServerResponse> findByOwnerId(ServerRequest request) {
        UUID ownerId = UUID.fromString(request.pathVariable("ownerId"));
        return ServerResponse.ok()
                .body(planUseCase.findByOwnerId(ownerId).map(PlanResponse::from), PlanResponse.class);
    }

    public Mono<ServerResponse> deactivate(ServerRequest request) {
        UUID planId = UUID.fromString(request.pathVariable("id"));
        return planUseCase.deactivate(planId)
                .map(PlanResponse::from)
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
