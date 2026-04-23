package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.*;
import com.ej.subscript.domain.repository.PaymentRepository;
import com.ej.subscript.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PaymentUseCaseTest {

    private SubscriptionRepository subscriptionRepository;
    private PaymentRepository paymentRepository;
    private PaymentUseCase paymentUseCase;

    private static final UUID REGISTERED_BY = UUID.randomUUID();
    private static final Money PRICE = new Money(new BigDecimal("80000"), "COP");
    private static final Subscription ACTIVE_SUB = Subscription.create(UUID.randomUUID(), UUID.randomUUID(), PRICE, 30);

    @BeforeEach
    void setUp() {
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        paymentRepository = Mockito.mock(PaymentRepository.class);
        paymentUseCase = new PaymentUseCase(subscriptionRepository, paymentRepository);
    }

    @Test
    void shouldRegisterPayment() {
        Payment payment = Payment.register(ACTIVE_SUB.id(), PRICE, REGISTERED_BY);
        when(subscriptionRepository.findById(ACTIVE_SUB.id())).thenReturn(Mono.just(ACTIVE_SUB));
        when(paymentRepository.save(any())).thenReturn(Mono.just(payment));

        StepVerifier.create(paymentUseCase.register(ACTIVE_SUB.id(), REGISTERED_BY))
                .assertNext(result -> {
                    assertThat(result.subscriptionId()).isEqualTo(ACTIVE_SUB.id());
                    assertThat(result.amount()).isEqualTo(PRICE);
                    assertThat(result.registeredBy()).isEqualTo(REGISTERED_BY);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturn404WhenSubscriptionNotFound() {
        UUID id = UUID.randomUUID();
        when(subscriptionRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(paymentUseCase.register(id, REGISTERED_BY))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(404);
                })
                .verify();
    }

    @Test
    void shouldFindPaymentsBySubscriptionId() {
        Payment payment = Payment.register(ACTIVE_SUB.id(), PRICE, REGISTERED_BY);
        when(paymentRepository.findBySubscriptionId(ACTIVE_SUB.id())).thenReturn(Flux.just(payment));

        StepVerifier.create(paymentUseCase.findBySubscriptionId(ACTIVE_SUB.id()))
                .assertNext(result -> assertThat(result.subscriptionId()).isEqualTo(ACTIVE_SUB.id()))
                .verifyComplete();
    }
}
