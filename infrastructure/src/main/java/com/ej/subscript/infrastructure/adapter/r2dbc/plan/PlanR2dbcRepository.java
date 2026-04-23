package com.ej.subscript.infrastructure.adapter.r2dbc.plan;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PlanR2dbcRepository extends ReactiveCrudRepository<PlanEntity, UUID> {
    Flux<PlanEntity> findByOwnerId(UUID ownerId);
}
