package com.ej.subscript.infrastructure.adapter.r2dbc.plan;

import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Plan;
import com.ej.subscript.domain.model.PlanStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlanMapperTest {

    private static final Plan PLAN = new Plan(
            UUID.randomUUID(), UUID.randomUUID(), "Mensual", "Acceso completo",
            new Money(new BigDecimal("80000"), "COP"), 30, PlanStatus.ACTIVE
    );

    @Test
    void shouldMapDomainToEntity() {
        PlanEntity entity = PlanMapper.toEntity(PLAN);

        assertThat(entity.getId()).isEqualTo(PLAN.id());
        assertThat(entity.getOwnerId()).isEqualTo(PLAN.ownerId());
        assertThat(entity.getPriceAmount()).isEqualTo(PLAN.price().amount());
        assertThat(entity.getPriceCurrency()).isEqualTo(PLAN.price().currency());
        assertThat(entity.getDurationDays()).isEqualTo(30);
        assertThat(entity.getStatus()).isEqualTo(PlanStatus.ACTIVE.name());
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void shouldMapDomainToEntityForUpdate() {
        PlanEntity entity = PlanMapper.toEntityForUpdate(PLAN);

        assertThat(entity.getId()).isEqualTo(PLAN.id());
        assertThat(entity.isNew()).isFalse();
    }

    @Test
    void shouldMapEntityToDomain() {
        PlanEntity entity = PlanMapper.toEntity(PLAN);
        Plan result = PlanMapper.toDomain(entity);

        assertThat(result.id()).isEqualTo(PLAN.id());
        assertThat(result.price().amount()).isEqualByComparingTo(new BigDecimal("80000"));
        assertThat(result.status()).isEqualTo(PlanStatus.ACTIVE);
    }

    @Test
    void shouldRoundTripWithoutDataLoss() {
        Plan result = PlanMapper.toDomain(PlanMapper.toEntity(PLAN));
        assertThat(result).isEqualTo(PLAN);
    }
}
