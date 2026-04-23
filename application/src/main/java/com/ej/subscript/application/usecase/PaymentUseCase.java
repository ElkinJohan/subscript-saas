package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Payment;
import com.ej.subscript.domain.repository.PaymentRepository;
import com.ej.subscript.domain.repository.SubscriptionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class PaymentUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    public PaymentUseCase(SubscriptionRepository subscriptionRepository,
                          PaymentRepository paymentRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
    }

    public Mono<Payment> register(UUID subscriptionId, UUID registeredBy) {
        return subscriptionRepository.findById(subscriptionId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Suscripción no encontrada", 404,
                        "No existe una suscripción con ID " + subscriptionId)))
                .map(sub -> Payment.register(sub.id(), sub.price(), registeredBy))
                .flatMap(paymentRepository::save);
    }

    public Flux<Payment> findBySubscriptionId(UUID subscriptionId) {
        return paymentRepository.findBySubscriptionId(subscriptionId);
    }
}
