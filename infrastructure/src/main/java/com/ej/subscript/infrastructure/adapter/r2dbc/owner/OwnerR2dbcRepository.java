package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OwnerR2dbcRepository extends ReactiveCrudRepository<OwnerEntity, UUID> {
    Mono<OwnerEntity> findByEmail(String email);
}
