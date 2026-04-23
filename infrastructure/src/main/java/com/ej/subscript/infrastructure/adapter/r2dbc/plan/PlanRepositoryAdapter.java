package com.ej.subscript.infrastructure.adapter.r2dbc.plan;

import com.ej.subscript.domain.model.Plan;
import com.ej.subscript.domain.repository.PlanRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class PlanRepositoryAdapter implements PlanRepository {

    private final PlanR2dbcRepository r2dbcRepository;

    public PlanRepositoryAdapter(PlanR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Mono<Plan> save(Plan plan) {
        return r2dbcRepository.save(PlanMapper.toEntity(plan))
                .map(PlanMapper::toDomain);
    }

    @Override
    public Mono<Plan> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(PlanMapper::toDomain);
    }

    @Override
    public Flux<Plan> findByOwnerId(UUID ownerId) {
        return r2dbcRepository.findByOwnerId(ownerId)
                .map(PlanMapper::toDomain);
    }
}
