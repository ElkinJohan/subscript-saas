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
                subscription.price().amount(), subscription.price().currency()
        );
    }

    static Subscription toDomain(SubscriptionEntity entity) {
        return new Subscription(
                entity.id(), entity.clientId(), entity.planId(),
                new SubscriptionPeriod(entity.startDate(), entity.endDate()),
                SubscriptionStatus.valueOf(entity.status()),
                new Money(entity.priceAmount(), entity.priceCurrency())
        );
    }
}
