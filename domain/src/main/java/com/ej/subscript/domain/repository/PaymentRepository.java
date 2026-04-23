package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Payment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Puerto de salida para la persistencia de {@link Payment}.
 * Los pagos son inmutables: solo se insertan, nunca se modifican.
 */
public interface PaymentRepository {

    /** Registra un nuevo pago (INSERT). */
    Mono<Payment> save(Payment payment);

    /** Retorna el historial de pagos de una suscripción, ordenado por fecha descendente en la implementación. */
    Flux<Payment> findBySubscriptionId(UUID subscriptionId);
}
