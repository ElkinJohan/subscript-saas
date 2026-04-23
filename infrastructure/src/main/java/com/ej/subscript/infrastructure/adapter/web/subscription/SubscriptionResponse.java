package com.ej.subscript.infrastructure.adapter.web.subscription;

import com.ej.subscript.domain.model.Subscription;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID clientId,
        UUID planId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        BigDecimal priceAmount,
        String priceCurrency
) {
    static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.id(), subscription.clientId(), subscription.planId(),
                subscription.period().startDate(), subscription.period().endDate(),
                subscription.status().name(),
                subscription.price().amount(), subscription.price().currency()
        );
    }
}
