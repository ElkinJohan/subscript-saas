package com.ej.subscript.infrastructure.adapter.r2dbc.subscription;

import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Subscription;
import com.ej.subscript.domain.model.SubscriptionPeriod;
import com.ej.subscript.domain.model.SubscriptionStatus;

class SubscriptionMapper {

    static SubscriptionEntity toEntity(Subscription subscription) {
        return new SubscriptionEntity(
                subscription.id(), subscription.clientId(), subscription.planId(),
                subscription.period().startDate(), subscription.period().endDate(),
                subscription.status().name(),
                subscription.price().amount(), subscription.price().currency(), true
        );
    }

    static SubscriptionEntity toEntityForUpdate(Subscription subscription) {
        return new SubscriptionEntity(
                subscription.id(), subscription.clientId(), subscription.planId(),
                subscription.period().startDate(), subscription.period().endDate(),
                subscription.status().name(),
                subscription.price().amount(), subscription.price().currency(), false
        );
    }

    static Subscription toDomain(SubscriptionEntity entity) {
        return new Subscription(
                entity.getId(), entity.getClientId(), entity.getPlanId(),
                new SubscriptionPeriod(entity.getStartDate(), entity.getEndDate()),
                SubscriptionStatus.valueOf(entity.getStatus()),
                new Money(entity.getPriceAmount(), entity.getPriceCurrency())
        );
    }
}
