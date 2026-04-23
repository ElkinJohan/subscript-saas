package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Payment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentRepository {
    Mono<Payment> save(Payment payment);
    Flux<Payment> findBySubscriptionId(UUID subscriptionId);
}
