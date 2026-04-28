package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Plan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Puerto de salida para la persistencia de {@link Plan}.
 * Ver {@code ClientRepository} para la explicación del patrón save/update separados.
 */
public interface PlanRepository {

    /**
     * Inserta un nuevo Plan (INSERT).
     */
    Mono<Plan> save(Plan plan);

    /**
     * Actualiza un Plan existente (UPDATE).
     */
    Mono<Plan> update(Plan plan);

    /**
     * Busca por ID; emite vacío si no existe.
     */
    Mono<Plan> findById(UUID id);

    /**
     * Retorna todos los planes del owner dado.
     */
    Flux<Plan> findByOwnerId(UUID ownerId);
}
