package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio Spring Data R2DBC de bajo nivel para {@link ClientEntity}.
 *
 * <p>Hereda los CRUD reactivos y agrega un derived query por {@code ownerId}
 * que se traduce automáticamente a {@code WHERE owner_id = ?}. El dominio no
 * conoce esta interfaz: el puente con el puerto {@code ClientRepository} lo
 * hace {@code ClientRepositoryAdapter}.
 */
public interface ClientR2dbcRepository extends ReactiveCrudRepository<ClientEntity, UUID> {

    /**
     * Devuelve todos los clientes con {@code owner_id} igual al parámetro.
     * Empty {@link Flux} si el owner no tiene clientes.
     */
    Flux<ClientEntity> findByOwnerId(UUID ownerId);

    /**
     * Devuelve el cliente con {@code owner_id} y {@code cedula} coincidentes.
     * Empty {@link Mono} si no existe. Respaldado por el UNIQUE constraint
     * compuesto {@code uk_clients_owner_cedula} a nivel DB.
     */
    Mono<ClientEntity> findByOwnerIdAndCedula(UUID ownerId, String cedula);
}
