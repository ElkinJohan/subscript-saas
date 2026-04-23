package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.*;
import com.ej.subscript.domain.repository.ClientRepository;
import com.ej.subscript.domain.repository.PlanRepository;
import com.ej.subscript.domain.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SubscriptionUseCaseTest {

    private SubscriptionRepository subscriptionRepository;
    private ClientRepository clientRepository;
    private PlanRepository planRepository;
    private SubscriptionUseCase subscriptionUseCase;

    private static final UUID CLIENT_ID = UUID.randomUUID();
    private static final UUID PLAN_ID = UUID.randomUUID();
    private static final Money PRICE = new Money(new BigDecimal("80000"), "COP");

    private static final Client CLIENT = new Client(
            CLIENT_ID, UUID.randomUUID(), "123", "Carlos", "carlos@test.com", "300", ClientStatus.ACTIVE
    );
    private static final Plan ACTIVE_PLAN = new Plan(
            PLAN_ID, UUID.randomUUID(), "Mensual", "Desc", PRICE, 30, PlanStatus.ACTIVE
    );
    private static final Plan INACTIVE_PLAN = new Plan(
            PLAN_ID, UUID.randomUUID(), "Mensual", "Desc", PRICE, 30, PlanStatus.INACTIVE
    );
    private static final Subscription ACTIVE_SUB = Subscription.create(CLIENT_ID, PLAN_ID, PRICE, 30);

    @BeforeEach
    void setUp() {
        subscriptionRepository = Mockito.mock(SubscriptionRepository.class);
        clientRepository = Mockito.mock(ClientRepository.class);
        planRepository = Mockito.mock(PlanRepository.class);
        subscriptionUseCase = new SubscriptionUseCase(subscriptionRepository, clientRepository, planRepository);
    }

    @Test
    void shouldCreateSubscription() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Mono.just(CLIENT));
        when(planRepository.findById(PLAN_ID)).thenReturn(Mono.just(ACTIVE_PLAN));
        when(subscriptionRepository.save(any())).thenReturn(Mono.just(ACTIVE_SUB));

        StepVerifier.create(subscriptionUseCase.create(CLIENT_ID, PLAN_ID))
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(SubscriptionStatus.ACTIVE);
                    assertThat(result.clientId()).isEqualTo(CLIENT_ID);
                })
                .verifyComplete();
    }

    @Test
    void shouldReturn404WhenClientNotFound() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Mono.empty());
        when(planRepository.findById(any())).thenReturn(Mono.empty()); // create() construye el Mono de plan eagerly

        StepVerifier.create(subscriptionUseCase.create(CLIENT_ID, PLAN_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(404);
                })
                .verify();
    }

    @Test
    void shouldReturn404WhenPlanNotFound() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Mono.just(CLIENT));
        when(planRepository.findById(PLAN_ID)).thenReturn(Mono.empty());

        StepVerifier.create(subscriptionUseCase.create(CLIENT_ID, PLAN_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(404);
                })
                .verify();
    }

    @Test
    void shouldReturn422WhenPlanIsInactive() {
        when(clientRepository.findById(CLIENT_ID)).thenReturn(Mono.just(CLIENT));
        when(planRepository.findById(PLAN_ID)).thenReturn(Mono.just(INACTIVE_PLAN));

        StepVerifier.create(subscriptionUseCase.create(CLIENT_ID, PLAN_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(422);
                })
                .verify();
    }

    @Test
    void shouldCancelSubscription() {
        Subscription cancelled = ACTIVE_SUB.cancel();
        when(subscriptionRepository.findById(ACTIVE_SUB.id())).thenReturn(Mono.just(ACTIVE_SUB));
        when(subscriptionRepository.update(any())).thenReturn(Mono.just(cancelled));

        StepVerifier.create(subscriptionUseCase.cancel(ACTIVE_SUB.id()))
                .assertNext(result -> assertThat(result.status()).isEqualTo(SubscriptionStatus.CANCELLED))
                .verifyComplete();
    }

    @Test
    void shouldReturn404WhenCancellingNonExistentSubscription() {
        UUID id = UUID.randomUUID();
        when(subscriptionRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(subscriptionUseCase.cancel(id))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(404);
                })
                .verify();
    }

    @Test
    void shouldReturn409WhenCancellingAlreadyCancelledSubscription() {
        Subscription cancelled = ACTIVE_SUB.cancel();
        when(subscriptionRepository.findById(cancelled.id())).thenReturn(Mono.just(cancelled));

        StepVerifier.create(subscriptionUseCase.cancel(cancelled.id()))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(409);
                })
                .verify();
    }

    @Test
    void shouldRenewSubscription() {
        Subscription renewed = ACTIVE_SUB.renew(30);
        when(subscriptionRepository.findById(ACTIVE_SUB.id())).thenReturn(Mono.just(ACTIVE_SUB));
        when(planRepository.findById(PLAN_ID)).thenReturn(Mono.just(ACTIVE_PLAN));
        when(subscriptionRepository.update(any())).thenReturn(Mono.just(renewed));

        StepVerifier.create(subscriptionUseCase.renew(ACTIVE_SUB.id()))
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(SubscriptionStatus.ACTIVE);
                    assertThat(result.period().startDate()).isEqualTo(ACTIVE_SUB.period().endDate());
                })
                .verifyComplete();
    }

    @Test
    void shouldFindSubscriptionsByClientId() {
        when(subscriptionRepository.findByClientId(CLIENT_ID)).thenReturn(Flux.just(ACTIVE_SUB));

        StepVerifier.create(subscriptionUseCase.findByClientId(CLIENT_ID))
                .assertNext(result -> assertThat(result.clientId()).isEqualTo(CLIENT_ID))
                .verifyComplete();
    }

    @Test
    void shouldReturn404WhenNoActiveSubscription() {
        when(subscriptionRepository.findActiveByClientId(CLIENT_ID)).thenReturn(Mono.empty());

        StepVerifier.create(subscriptionUseCase.findActiveByClientId(CLIENT_ID))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(404);
                })
                .verify();
    }
}
