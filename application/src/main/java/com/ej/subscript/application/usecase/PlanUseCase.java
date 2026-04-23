package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Plan;
import com.ej.subscript.domain.repository.PlanRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Orquesta los casos de uso del Plan.
 * Clase Java pura — sin anotaciones de Spring. Bean registrado en {@code BeanConfiguration}.
 */
public class PlanUseCase {

    private final PlanRepository planRepository;

    public PlanUseCase(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /** Crea un nuevo plan. */
    public Mono<Plan> create(Plan plan) {
        return planRepository.save(plan);
    }

    /** Desactiva el plan o emite 404 si no existe. Los planes inactivos no pueden ser suscritos. */
    public Mono<Plan> deactivate(UUID planId) {
        return planRepository.findById(planId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Plan no encontrado", 404,
                        "No existe un plan con ID " + planId)))
                .map(Plan::deactivate)
                .flatMap(planRepository::update);
    }

    /** Retorna todos los planes del owner dado. */
    public Flux<Plan> findByOwnerId(UUID ownerId) {
        return planRepository.findByOwnerId(ownerId);
    }
}
