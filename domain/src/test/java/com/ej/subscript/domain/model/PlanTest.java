package com.ej.subscript.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final Money PRICE = Money.cop(new BigDecimal("50000"));

    @Test
    void shouldCreatePlanSuccessfully() {
        Plan plan = new Plan(UUID.randomUUID(), OWNER_ID, "Mensual", "Plan básico", PRICE, 30, PlanStatus.ACTIVE);

        assertThat(plan.name()).isEqualTo("Mensual");
        assertThat(plan.durationDays()).isEqualTo(30);
        assertThat(plan.status()).isEqualTo(PlanStatus.ACTIVE);
    }

    @Test
    void shouldCreateWithFactory() {
        Plan plan = Plan.create(OWNER_ID, "Mensual", "Plan básico", PRICE, 30);

        assertThat(plan.id()).isNotNull();
        assertThat(plan.status()).isEqualTo(PlanStatus.ACTIVE);
        assertThat(plan.ownerId()).isEqualTo(OWNER_ID);
    }

    @Test
    void shouldDeactivatePlan() {
        Plan plan = Plan.create(OWNER_ID, "Mensual", "Plan básico", PRICE, 30);

        Plan deactivated = plan.deactivate();

        assertThat(deactivated.status()).isEqualTo(PlanStatus.INACTIVE);
    }

    @Test
    void shouldActivatePlan() {
        Plan plan = Plan.create(OWNER_ID, "Mensual", "Plan básico", PRICE, 30);
        Plan deactivated = plan.deactivate();

        Plan activated = deactivated.activate();

        assertThat(activated.status()).isEqualTo(PlanStatus.ACTIVE);
    }

    @Test
    void shouldBeImmutableOnDeactivate() {
        Plan plan = Plan.create(OWNER_ID, "Mensual", "Plan básico", PRICE, 30);

        Plan deactivated = plan.deactivate();

        assertThat(deactivated).isNotSameAs(plan);
        assertThat(plan.status()).isEqualTo(PlanStatus.ACTIVE);
    }

    @Test
    void shouldThrowWhenOwnerIdIsNull() {
        assertThatThrownBy(() -> Plan.create(null, "Mensual", "desc", PRICE, 30))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> Plan.create(OWNER_ID, "  ", "desc", PRICE, 30))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenPriceIsNull() {
        assertThatThrownBy(() -> Plan.create(OWNER_ID, "Mensual", "desc", null, 30))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenDurationIsZero() {
        assertThatThrownBy(() -> Plan.create(OWNER_ID, "Mensual", "desc", PRICE, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
