package com.ej.subscript.infrastructure.adapter.r2dbc.payment;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface PaymentR2dbcRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {
    Flux<PaymentEntity> findBySubscriptionId(UUID subscriptionId);
}
