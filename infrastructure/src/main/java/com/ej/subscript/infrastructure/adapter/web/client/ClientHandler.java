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

    /**
     * Registra un Client nuevo bajo el {@code ownerId} del path.
     *
     * <p>El {@code ownerId} se toma del path —no del body— para que la relación
     * padre-hijo sea inequívoca y futuras reglas de autorización a nivel de
     * fila puedan compararlo contra el owner del token sin parsear payloads.
     *
     * @return {@code 201 Created} con el Client persistido en estado {@code ACTIVE}.
     *         Errores: {@code 400} validación de schema, {@code 401} sin token,
     *         {@code 422} invariantes de dominio.
     */
    public Mono<ServerResponse> register(ServerRequest request) {
        UUID ownerId = UUID.fromString(request.pathVariable("ownerId"));
        return request.bodyToMono(ClientRequest.class)
                .flatMap(this::validate)
                .map(req -> Client.create(ownerId, req.cedula(), req.name(), req.email(), req.phone()))
                .flatMap(clientUseCase::register)
                .map(ClientResponse::from)
                .flatMap(body -> ServerResponse.status(HttpStatus.CREATED).bodyValue(body));
    }

    /**
     * Lista todos los clientes del Owner (activos e inactivos).
     *
     * <p>Stream-friendly: la respuesta es un array JSON construido a partir del
     * {@link reactor.core.publisher.Flux} del use case. Si el owner no tiene
     * clientes, la respuesta es un array vacío con {@code 200 OK}, no un 404.
     */
    public Mono<ServerResponse> findByOwnerId(ServerRequest request) {
        UUID ownerId = UUID.fromString(request.pathVariable("ownerId"));
        return ServerResponse.ok()
                .body(clientUseCase.findByOwnerId(ownerId).map(ClientResponse::from), ClientResponse.class);
    }

    /**
     * Desactiva el Client referenciado en el path: {@code status} pasa a
     * {@code INACTIVE} sin borrar el registro.
     *
     * <p>El path lleva tanto {@code ownerId} como {@code clientId} para que la
     * relación padre-hijo sea explícita y la autorización a nivel de fila se
     * pueda validar contra el token sin tocar la DB.
     *
     * <p>Idempotente: llamar dos veces deja el Client en el mismo estado.
     * Útil para que clientes sean "archivados" sin perder histórico para
     * reportes o auditoría futura.
     *
     * @return {@code 200 OK} con el Client actualizado; {@code 404} si no existe;
     *         {@code 401} si el token está ausente o es inválido.
     */
    public Mono<ServerResponse> deactivate(ServerRequest request) {
        UUID clientId = UUID.fromString(request.pathVariable("clientId"));
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
