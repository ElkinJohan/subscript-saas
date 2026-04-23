package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SubscriptionRepository {
    Mono<Subscription> save(Subscription subscription);
    Mono<Subscription> update(Subscription subscription);
    Mono<Subscription> findById(UUID id);
    Flux<Subscription> findByClientId(UUID clientId);
    Mono<Subscription> findActiveByClientId(UUID clientId);
}
