package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Puerto de salida para la persistencia de {@link Client}.
 *
 * <p>{@code save} y {@code update} están separados deliberadamente:
 * R2DBC determina INSERT vs UPDATE según si el ID es nulo. Como el dominio
 * genera el UUID antes de persistir, ambos métodos señalan la intención
 * explícita en lugar de depender de heurísticas de la capa de datos.
 */
public interface ClientRepository {

    /**
     * Inserta un nuevo Client (INSERT).
     */
    Mono<Client> save(Client client);

    /**
     * Actualiza un Client existente (UPDATE).
     */
    Mono<Client> update(Client client);

    /**
     * Busca por ID; emite vacío si no existe.
     */
    Mono<Client> findById(UUID id);

    /**
     * Retorna todos los clientes del owner dado.
     */
    Flux<Client> findByOwnerId(UUID ownerId);
}
