package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Plan;
import com.ej.subscript.domain.model.PlanStatus;
import com.ej.subscript.domain.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class PlanUseCaseTest {

    private PlanRepository planRepository;
    private PlanUseCase planUseCase;

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final Plan ACTIVE_PLAN = new Plan(
            UUID.randomUUID(), OWNER_ID, "Mensual", "Acceso completo",
            new Money(new BigDecimal("80000"), "COP"), 30, PlanStatus.ACTIVE
    );

    @BeforeEach
    void setUp() {
        planRepository = Mockito.mock(PlanRepository.class);
        planUseCase = new PlanUseCase(planRepository);
    }

    @Test
    void shouldCreatePlan() {
        when(planRepository.save(any())).thenReturn(Mono.just(ACTIVE_PLAN));

        StepVerifier.create(planUseCase.create(ACTIVE_PLAN))
                .assertNext(result -> {
                    assertThat(result.name()).isEqualTo("Mensual");
                    assertThat(result.status()).isEqualTo(PlanStatus.ACTIVE);
                })
                .verifyComplete();
    }

    @Test
    void shouldDeactivatePlan() {
        Plan inactive = new Plan(
                ACTIVE_PLAN.id(), OWNER_ID, "Mensual", "Acceso completo",
                ACTIVE_PLAN.price(), 30, PlanStatus.INACTIVE
        );
        when(planRepository.findById(ACTIVE_PLAN.id())).thenReturn(Mono.just(ACTIVE_PLAN));
        when(planRepository.update(any())).thenReturn(Mono.just(inactive));

        StepVerifier.create(planUseCase.deactivate(ACTIVE_PLAN.id()))
                .assertNext(result -> assertThat(result.status()).isEqualTo(PlanStatus.INACTIVE))
                .verifyComplete();
    }

    @Test
    void shouldReturn404WhenDeactivatingNonExistentPlan() {
        UUID id = UUID.randomUUID();
        when(planRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(planUseCase.deactivate(id))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(404);
                })
                .verify();
    }

    @Test
    void shouldFindPlansByOwnerId() {
        when(planRepository.findByOwnerId(OWNER_ID)).thenReturn(Flux.just(ACTIVE_PLAN));

        StepVerifier.create(planUseCase.findByOwnerId(OWNER_ID))
                .assertNext(result -> assertThat(result.ownerId()).isEqualTo(OWNER_ID))
                .verifyComplete();
    }
}
