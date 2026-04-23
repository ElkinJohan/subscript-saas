package com.ej.subscript.infrastructure.adapter.web.plan;

import com.ej.subscript.domain.model.Plan;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        BigDecimal priceAmount,
        String priceCurrency,
        int durationDays,
        String status
) {
    static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.id(), plan.ownerId(), plan.name(), plan.description(),
                plan.price().amount(), plan.price().currency(),
                plan.durationDays(), plan.status().name()
        );
    }
}
