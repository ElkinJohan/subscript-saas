package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Owner;
import reactor.core.publisher.Mono;

/**
 * Puerto de salida (output port) para la persistencia de {@link Owner}.
 * Define QUÉ operaciones necesita el dominio, sin acoplar al mecanismo de almacenamiento.
 * La implementación concreta vive en la capa de infraestructura ({@code OwnerRepositoryAdapter}).
 */
public interface OwnerRepository {

    /** Persiste un nuevo Owner (INSERT). */
    Mono<Owner> save(Owner owner);

    /** Busca por ID; emite vacío si no existe. */
    Mono<Owner> findById(String id);

    /** Busca por email; emite vacío si no existe. Usado para validar unicidad y para login. */
    Mono<Owner> findByEmail(String email);
}
