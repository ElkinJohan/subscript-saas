package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Outbound port for persisting {@link Client}.
 *
 * <p>{@code save} and {@code update} are intentionally separate: R2DBC
 * decides INSERT vs UPDATE based on whether the id is null. Since the
 * domain generates the UUID before persisting, both methods signal the
 * explicit intent instead of relying on data-layer heuristics.
 */
public interface ClientRepository {

    /**
     * Inserts a new Client (INSERT).
     */
    Mono<Client> save(Client client);

    /**
     * Updates an existing Client (UPDATE).
     */
    Mono<Client> update(Client client);

    /**
     * Looks up by id; emits empty when absent.
     */
    Mono<Client> findById(UUID id);

    /**
     * Returns every client belonging to the given owner.
     */
    Flux<Client> findByOwnerId(UUID ownerId);

    /**
     * Looks up a client by the {@code (ownerId, cedula)} pair; emits empty
     * when absent. Used to validate per-owner uniqueness before save —
     * a cedula can repeat across owners but never within the same owner.
     */
    Mono<Client> findByOwnerIdAndCedula(UUID ownerId, String cedula);
}
