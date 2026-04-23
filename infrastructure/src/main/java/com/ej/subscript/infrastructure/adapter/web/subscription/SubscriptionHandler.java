package com.ej.subscript.infrastructure.adapter.web.subscription;

import com.ej.subscript.application.usecase.SubscriptionUseCase;
import com.ej.subscript.domain.exception.BusinessException;
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
public class SubscriptionHandler {

    private final SubscriptionUseCase subscriptionUseCase;
    private final Validator validator;

    public SubscriptionHandler(SubscriptionUseCase subscriptionUseCase, Validator validator) {
        this.subscriptionUseCase = subscriptionUseCase;
        this.validator = validator;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(SubscriptionRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> subscriptionUseCase.create(req.clientId(), req.planId()))
                .map(SubscriptionResponse::from)
                .flatMap(body -> ServerResponse.status(HttpStatus.CREATED).bodyValue(body));
    }

    public Mono<ServerResponse> findByClientId(ServerRequest request) {
        UUID clientId = UUID.fromString(request.pathVariable("clientId"));
        return ServerResponse.ok()
                .body(subscriptionUseCase.findByClientId(clientId).map(SubscriptionResponse::from),
                        SubscriptionResponse.class);
    }

    public Mono<ServerResponse> findActiveByClientId(ServerRequest request) {
        UUID clientId = UUID.fromString(request.pathVariable("clientId"));
        return subscriptionUseCase.findActiveByClientId(clientId)
                .map(SubscriptionResponse::from)
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    public Mono<ServerResponse> cancel(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return subscriptionUseCase.cancel(id)
                .map(SubscriptionResponse::from)
                .flatMap(body -> ServerResponse.ok().bodyValue(body));
    }

    public Mono<ServerResponse> renew(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return subscriptionUseCase.renew(id)
                .map(SubscriptionResponse::from)
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
