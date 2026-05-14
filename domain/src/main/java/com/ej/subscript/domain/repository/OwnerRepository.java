package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Owner;
import reactor.core.publisher.Mono;

/**
 * Outbound port for persisting {@link Owner}.
 * Declares WHAT operations the domain needs without coupling to a storage
 * mechanism. The concrete implementation lives in the infrastructure layer
 * ({@code OwnerRepositoryAdapter}).
 */
public interface OwnerRepository {

    /**
     * Persists a new Owner (INSERT).
     */
    Mono<Owner> save(Owner owner);

    /**
     * Looks up by id; emits empty when absent.
     */
    Mono<Owner> findById(String id);

    /**
     * Looks up by email; emits empty when absent. Used both to validate
     * uniqueness on register and as the login credential lookup.
     */
    Mono<Owner> findByEmail(String email);

    /**
     * Looks up by NIT; emits empty when absent. Used to validate uniqueness
     * on register.
     */
    Mono<Owner> findByNit(String nit);

    /**
     * Removes the Owner by id. Completes empty on success.
     */
    Mono<Void> deleteById(String id);
}
