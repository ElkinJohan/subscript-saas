package com.ej.subscript.infrastructure.adapter.web.payment;

import com.ej.subscript.application.usecase.PaymentUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Handler funcional para los endpoints de Payment.
 * Adaptador de entrada que traduce el request HTTP al modelo de dominio
 * y delega al {@link PaymentUseCase}.
 */
@Component
@RequiredArgsConstructor
public class PaymentHandler {

    private final PaymentUseCase paymentUseCase;
    private final Validator validator;

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(PaymentRequest.class)
                .flatMap(this::validate)
                .flatMap(req -> paymentUseCase.register(req.subscriptionId(), req.registeredBy()))
                .map(PaymentResponse::from)
                .flatMap(body -> ServerResponse.status(HttpStatus.CREATED).bodyValue(body));
    }

    public Mono<ServerResponse> findBySubscriptionId(ServerRequest request) {
        UUID subscriptionId = UUID.fromString(request.pathVariable("subscriptionId"));
        return ServerResponse.ok()
                .body(paymentUseCase.findBySubscriptionId(subscriptionId).map(PaymentResponse::from),
                        PaymentResponse.class);
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
