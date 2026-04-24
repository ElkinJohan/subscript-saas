package com.ej.subscript.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.ej.subscript.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SubscriptionTest {

    private static final UUID CLIENT_ID = UUID.randomUUID();
    private static final UUID PLAN_ID = UUID.randomUUID();
    private static final Money PRICE = Money.cop(new BigDecimal("50000"));

    @Test
    void shouldCreateSubscriptionSuccessfully() {
        Subscription sub = Subscription.create(CLIENT_ID, PLAN_ID, PRICE, 30);

        assertThat(sub.id()).isNotNull();
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(sub.clientId()).isEqualTo(CLIENT_ID);
        assertThat(sub.planId()).isEqualTo(PLAN_ID);
    }

    @Test
    void shouldCancelSubscription() {
        Subscription sub = Subscription.create(CLIENT_ID, PLAN_ID, PRICE, 30);

        Subscription cancelled = sub.cancel();

        assertThat(cancelled.status()).isEqualTo(SubscriptionStatus.CANCELLED);
    }

    @Test
    void shouldBeImmutableOnCancel() {
        Subscription sub = Subscription.create(CLIENT_ID, PLAN_ID, PRICE, 30);

        Subscription cancelled = sub.cancel();

        assertThat(cancelled).isNotSameAs(sub);
        assertThat(sub.status()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void shouldRenewSubscriptionFromEndDate() {
        Subscription sub = Subscription.create(CLIENT_ID, PLAN_ID, PRICE, 30);
        LocalDate expectedStart = sub.period().endDate();

        Subscription renewed = sub.renew(30);

        assertThat(renewed.period().startDate()).isEqualTo(expectedStart);
        assertThat(renewed.status()).isEqualTo(SubscriptionStatus.ACTIVE);
    }

    @Test
    void shouldDetectExpiredSubscription() {
        SubscriptionPeriod expiredPeriod = SubscriptionPeriod.of(LocalDate.now().minusDays(40), 30);
        Subscription sub = new Subscription(UUID.randomUUID(), CLIENT_ID, PLAN_ID, expiredPeriod, SubscriptionStatus.ACTIVE, PRICE);

        assertThat(sub.isExpired()).isTrue();
    }

    @Test
    void shouldDetectSubscriptionDueForRenewal() {
        SubscriptionPeriod nearPeriod = SubscriptionPeriod.of(LocalDate.now(), 5);
        Subscription sub = new Subscription(UUID.randomUUID(), CLIENT_ID, PLAN_ID, nearPeriod, SubscriptionStatus.ACTIVE, PRICE);

        assertThat(sub.isDueForRenewal(7)).isTrue();
    }

    @Test
    void shouldThrowWhenClientIdIsNull() {
        assertThatThrownBy(() -> Subscription.create(null, PLAN_ID, PRICE, 30))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenPlanIdIsNull() {
        assertThatThrownBy(() -> Subscription.create(CLIENT_ID, null, PRICE, 30))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenPriceIsNull() {
        assertThatThrownBy(() -> Subscription.create(CLIENT_ID, PLAN_ID, null, 30))
                .isInstanceOf(BusinessException.class);
    }
}
