package com.ej.subscript.infrastructure.adapter.web.client;

import com.ej.subscript.application.usecase.ClientUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Client;
import com.ej.subscript.infrastructure.security.AuthenticatedOwnerResolver;
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
 * Functional handler for the Client endpoints.
 * Acts as an inbound adapter (Hexagonal): translates the HTTP request
 * into the domain model and delegates to {@link ClientUseCase}.
 */
@Component
@RequiredArgsConstructor
public class ClientHandler {

    private final ClientUseCase clientUseCase;
    private final Validator validator;
    private final AuthenticatedOwnerResolver authenticatedOwnerResolver;

    /**
     * Registers a new Client under the path's {@code ownerId}.
     *
     * <p>The {@code ownerId} comes from the path — not from the body — so
     * the parent-child relation is unambiguous and the row-level
     * authorization rule (token caller == resource owner) is evaluated
     * against the path without parsing the body.
     *
     * @return {@code 201 Created} with the persisted Client in
     *         {@code ACTIVE} state. Errors: {@code 400} schema validation,
     *         {@code 401} missing token, {@code 403} caller does not own
     *         the path's owner, {@code 422} domain invariants.
     */
    public Mono<ServerResponse> register(ServerRequest request) {
        return requireOwnerMatchesCaller(request)
                .flatMap(ownerId -> request.bodyToMono(ClientRequest.class)
                        .flatMap(this::validate)
                        .map(req -> Client.create(ownerId, req.cedula(), req.name(), req.email(), req.phone()))
                        .flatMap(clientUseCase::register)
                        .map(ClientResponse::from)
                        .flatMap(body -> ServerResponse.status(HttpStatus.CREATED).bodyValue(body)));
    }

    /**
     * Lists every client of the Owner (active and inactive).
     *
     * <p>Stream-friendly: the response is a JSON array built from the use
     * case's {@link reactor.core.publisher.Flux}. If the owner has no
     * clients, the response is an empty array with {@code 200 OK}, not a
     * 404.
     *
     * @return {@code 200 OK} with the list; {@code 401} missing token;
     *         {@code 403} caller does not own the path's owner.
     */
    public Mono<ServerResponse> findByOwnerId(ServerRequest request) {
        return requireOwnerMatchesCaller(request)
                .flatMap(ownerId -> ServerResponse.ok()
                        .body(clientUseCase.findByOwnerId(ownerId).map(ClientResponse::from),
                                ClientResponse.class));
    }

    /**
     * Deactivates the Client referenced by the path: {@code status} flips
     * to {@code INACTIVE} without deleting the record.
     *
     * <p>The path carries both {@code ownerId} and {@code clientId} so the
     * parent-child relation is explicit and row-level authorization can
     * be validated against the token without touching the DB.
     *
     * <p>Idempotent: calling twice leaves the Client in the same state.
     * Useful so clients can be "archived" without losing history for
     * later reports or audit.
     *
     * @return {@code 200 OK} with the updated Client; {@code 401} missing
     *         token; {@code 403} caller does not own the path's owner;
     *         {@code 404} when the client does not exist.
     */
    public Mono<ServerResponse> deactivate(ServerRequest request) {
        return requireOwnerMatchesCaller(request)
                .flatMap(ownerId -> {
                    UUID clientId = UUID.fromString(request.pathVariable("clientId"));
                    return clientUseCase.deactivate(clientId)
                            .map(ClientResponse::from)
                            .flatMap(body -> ServerResponse.ok().bodyValue(body));
                });
    }

    /**
     * Activates the Client referenced by the path: {@code status} flips
     * back to {@code ACTIVE} so the client shows up again on listings and
     * is eligible for new subscriptions.
     *
     * <p>Mirror of {@link #deactivate(ServerRequest)}: same nested URL,
     * same row-level authorization rule, same idempotent behavior
     * (calling twice leaves the Client in {@code ACTIVE}).
     *
     * @return {@code 200 OK} with the updated Client; {@code 401} missing
     *         token; {@code 403} caller does not own the path's owner;
     *         {@code 404} when the client does not exist.
     */
    public Mono<ServerResponse> activate(ServerRequest request) {
        return requireOwnerMatchesCaller(request)
                .flatMap(ownerId -> {
                    UUID clientId = UUID.fromString(request.pathVariable("clientId"));
                    return clientUseCase.activate(clientId)
                            .map(ClientResponse::from)
                            .flatMap(body -> ServerResponse.ok().bodyValue(body));
                });
    }

    /**
     * Authorization gate: ensures the path's {@code ownerId} matches the
     * caller's {@code ownerId} (extracted from the JWT by
     * {@link AuthenticatedOwnerResolver}). Returns the validated ownerId
     * so the rest of the flow can reuse it without reparsing the path.
     *
     * @return the validated ownerId, or a {@code 403 Forbidden} error
     *         when there is a mismatch.
     */
    private Mono<UUID> requireOwnerMatchesCaller(ServerRequest request) {
        UUID pathOwnerId = UUID.fromString(request.pathVariable("ownerId"));
        return authenticatedOwnerResolver.currentOwnerId(request)
                .flatMap(callerOwnerId -> callerOwnerId.equals(pathOwnerId)
                        ? Mono.just(pathOwnerId)
                        : Mono.error(new BusinessException(
                                "Access denied", 403,
                                "Token owner does not match the resource owner")));
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
