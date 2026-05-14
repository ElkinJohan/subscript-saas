package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio Spring Data R2DBC de bajo nivel para {@link OwnerEntity}.
 *
 * <p>Hereda los CRUD reactivos de {@link ReactiveCrudRepository} y agrega un
 * derived query por email. El dominio no depende de esta interfaz: el puente
 * con el puerto {@code OwnerRepository} lo hace {@code OwnerRepositoryAdapter}.
 */
public interface OwnerR2dbcRepository extends ReactiveCrudRepository<OwnerEntity, UUID> {

    /**
     * Devuelve el Owner cuyo email coincida exactamente. Emite vacío si no existe.
     */
    Mono<OwnerEntity> findByEmail(String email);
}
