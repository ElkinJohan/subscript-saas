package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ClientR2dbcRepository extends ReactiveCrudRepository<ClientEntity, UUID> {
    Flux<ClientEntity> findByOwnerId(UUID ownerId);
}
