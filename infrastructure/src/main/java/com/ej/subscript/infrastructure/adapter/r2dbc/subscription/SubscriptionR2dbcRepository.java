package com.ej.subscript.infrastructure.adapter.r2dbc.subscription;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubscriptionR2dbcRepository extends ReactiveCrudRepository<SubscriptionEntity, UUID> {
    Flux<SubscriptionEntity> findByClientId(UUID clientId);

    @Query("SELECT * FROM subscriptions WHERE client_id = :clientId AND status = 'ACTIVE' LIMIT 1")
    Mono<SubscriptionEntity> findActiveByClientId(UUID clientId);
}
