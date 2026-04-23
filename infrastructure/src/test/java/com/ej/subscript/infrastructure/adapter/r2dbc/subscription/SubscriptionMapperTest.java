package com.ej.subscript.infrastructure.adapter.r2dbc.subscription;

import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Subscription;
import com.ej.subscript.domain.model.SubscriptionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionMapperTest {

    private static final Money PRICE = new Money(new BigDecimal("80000"), "COP");
    private static final Subscription SUBSCRIPTION = Subscription.create(UUID.randomUUID(), UUID.randomUUID(), PRICE, 30);

    @Test
    void shouldMapDomainToEntity() {
        SubscriptionEntity entity = SubscriptionMapper.toEntity(SUBSCRIPTION);

        assertThat(entity.getId()).isEqualTo(SUBSCRIPTION.id());
        assertThat(entity.getClientId()).isEqualTo(SUBSCRIPTION.clientId());
        assertThat(entity.getPlanId()).isEqualTo(SUBSCRIPTION.planId());
        assertThat(entity.getStartDate()).isEqualTo(SUBSCRIPTION.period().startDate());
        assertThat(entity.getEndDate()).isEqualTo(SUBSCRIPTION.period().endDate());
        assertThat(entity.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE.name());
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void shouldMapDomainToEntityForUpdate() {
        SubscriptionEntity entity = SubscriptionMapper.toEntityForUpdate(SUBSCRIPTION);

        assertThat(entity.getId()).isEqualTo(SUBSCRIPTION.id());
        assertThat(entity.isNew()).isFalse();
    }

    @Test
    void shouldMapEntityToDomain() {
        SubscriptionEntity entity = SubscriptionMapper.toEntity(SUBSCRIPTION);
        Subscription result = SubscriptionMapper.toDomain(entity);

        assertThat(result.id()).isEqualTo(SUBSCRIPTION.id());
        assertThat(result.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.price()).isEqualTo(PRICE);
    }

    @Test
    void shouldRoundTripWithoutDataLoss() {
        Subscription result = SubscriptionMapper.toDomain(SubscriptionMapper.toEntity(SUBSCRIPTION));
        assertThat(result).isEqualTo(SUBSCRIPTION);
    }
}
