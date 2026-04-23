package com.ej.subscript.infrastructure.adapter.web.client;

import com.ej.subscript.application.usecase.ClientUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Client;
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
 * Handler funcional para los endpoints de Client.
 * Opera como adaptador de entrada (Hexagonal): traduce el request HTTP
 * al modelo de dominio y delega al {@link ClientUseCase}.
 */
@Component
@RequiredArgsConstructor
public class ClientHandler {

    private final ClientUseCase clientUseCase;
    private final Validator validator;

    public Mono<ServerResponse> register(ServerRequest request) {
        UUID ownerId = UUID.fromString(request.pathVariable("ownerId"));
        return request.bodyToMono(ClientRequest.class)
                .flatMap(this::validate)
                .map(req -> Client.create(ownerId, req.cedula(), req.name(), req.email(), req.phone()))
                .flatMap(clientUseCase::register)
                .map(ClientResponse::from)
                .flatMap(body -> ServerResponse.status(HttpStatus.CREATED).bodyValue(body));
    }

    public Mono<ServerResponse> findByOwnerId(ServerRequest request) {
        UUID ownerId = UUID.fromString(request.pathVariable("ownerId"));
        return ServerResponse.ok()
                .body(clientUseCase.findByOwnerId(ownerId).map(ClientResponse::from), ClientResponse.class);
    }

    public Mono<ServerResponse> deactivate(ServerRequest request) {
        UUID clientId = UUID.fromString(request.pathVariable("id"));
        return clientUseCase.deactivate(clientId)
                .map(ClientResponse::from)
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
