package com.ej.subscript.infrastructure.adapter.r2dbc.payment;

import com.ej.subscript.domain.model.Payment;
import com.ej.subscript.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentR2dbcRepository r2dbcRepository;

    @Override
    public Mono<Payment> save(Payment payment) {
        return r2dbcRepository.save(PaymentMapper.toEntity(payment))
                .map(PaymentMapper::toDomain);
    }

    @Override
    public Flux<Payment> findBySubscriptionId(UUID subscriptionId) {
        return r2dbcRepository.findBySubscriptionId(subscriptionId)
                .map(PaymentMapper::toDomain);
    }
}
