package com.ej.subscript.infrastructure.adapter.r2dbc.plan;

import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Plan;
import com.ej.subscript.domain.model.PlanStatus;

class PlanMapper {

    static PlanEntity toEntity(Plan plan) {
        return new PlanEntity(
                plan.id(), plan.ownerId(), plan.name(), plan.description(),
                plan.price().amount(), plan.price().currency(),
                plan.durationDays(), plan.status().name()
        );
    }

    static Plan toDomain(PlanEntity entity) {
        return new Plan(
                entity.id(), entity.ownerId(), entity.name(), entity.description(),
                new Money(entity.priceAmount(), entity.priceCurrency()),
                entity.durationDays(), PlanStatus.valueOf(entity.status())
        );
    }
}
