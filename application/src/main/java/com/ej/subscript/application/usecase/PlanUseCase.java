package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Plan;
import com.ej.subscript.domain.repository.PlanRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class PlanUseCase {

    private final PlanRepository planRepository;

    public PlanUseCase(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public Mono<Plan> create(Plan plan) {
        return planRepository.save(plan);
    }

    public Mono<Plan> deactivate(UUID planId) {
        return planRepository.findById(planId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Plan no encontrado", 404,
                        "No existe un plan con ID " + planId)))
                .map(Plan::deactivate)
                .flatMap(planRepository::save);
    }

    public Flux<Plan> findByOwnerId(UUID ownerId) {
        return planRepository.findByOwnerId(ownerId);
    }
}
