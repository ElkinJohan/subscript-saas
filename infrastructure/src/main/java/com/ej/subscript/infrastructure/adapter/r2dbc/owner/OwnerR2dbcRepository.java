package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Low-level Spring Data R2DBC repository for {@link OwnerEntity}.
 *
 * <p>Inherits reactive CRUD from {@link ReactiveCrudRepository} and adds
 * derived queries by email and NIT. The domain does not depend on this
 * interface: the bridge to the {@code OwnerRepository} port is
 * {@code OwnerRepositoryAdapter}.
 */
public interface OwnerR2dbcRepository extends ReactiveCrudRepository<OwnerEntity, UUID> {

    /**
     * Returns the Owner whose email matches exactly. Emits empty when absent.
     */
    Mono<OwnerEntity> findByEmail(String email);

    /**
     * Returns the Owner whose NIT matches exactly. Emits empty when absent.
     */
    Mono<OwnerEntity> findByNit(String nit);
}
