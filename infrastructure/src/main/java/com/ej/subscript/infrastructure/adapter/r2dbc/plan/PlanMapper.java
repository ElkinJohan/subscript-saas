package com.ej.subscript.infrastructure.adapter.r2dbc.plan;

import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Plan;
import com.ej.subscript.domain.model.PlanStatus;

class PlanMapper {

    static PlanEntity toEntity(Plan plan) {
        return new PlanEntity(
                plan.id(), plan.ownerId(), plan.name(), plan.description(),
                plan.price().amount(), plan.price().currency(),
                plan.durationDays(), plan.status().name(), true
        );
    }

    static PlanEntity toEntityForUpdate(Plan plan) {
        return new PlanEntity(
                plan.id(), plan.ownerId(), plan.name(), plan.description(),
                plan.price().amount(), plan.price().currency(),
                plan.durationDays(), plan.status().name(), false
        );
    }

    static Plan toDomain(PlanEntity entity) {
        return new Plan(
                entity.getId(), entity.getOwnerId(), entity.getName(), entity.getDescription(),
                new Money(entity.getPriceAmount(), entity.getPriceCurrency()),
                entity.getDurationDays(), PlanStatus.valueOf(entity.getStatus())
        );
    }
}
