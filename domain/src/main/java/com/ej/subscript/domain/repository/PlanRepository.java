package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Plan;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PlanRepository {
    Mono<Plan> save(Plan plan);
    Mono<Plan> findById(UUID id);
    Flux<Plan> findByOwnerId(UUID ownerId);
}
