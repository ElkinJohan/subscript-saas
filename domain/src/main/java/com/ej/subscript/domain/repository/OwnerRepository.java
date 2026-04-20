package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Owner;
import reactor.core.publisher.Mono;

/**
 * Interfaz de repositorio (Puerto de salida).
 * Define qué queremos hacer con los datos, pero no cómo se hace.
 */
public interface OwnerRepository {
    Mono<Owner> save(Owner owner);

    Mono<Owner> findById(String id);

    Mono<Owner> findByEmail(String email);
}