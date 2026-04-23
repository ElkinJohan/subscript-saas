package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.domain.repository.OwnerRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class OwnerRepositoryAdapter implements OwnerRepository {

    private final OwnerR2dbcRepository r2dbcRepository;

    public OwnerRepositoryAdapter(OwnerR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Mono<Owner> save(Owner owner) {
        return r2dbcRepository.save(OwnerMapper.toEntity(owner))
                .map(OwnerMapper::toDomain);
    }

    @Override
    public Mono<Owner> findById(String id) {
        return r2dbcRepository.findById(UUID.fromString(id))
                .map(OwnerMapper::toDomain);
    }

    @Override
    public Mono<Owner> findByEmail(String email) {
        return r2dbcRepository.findByEmail(email)
                .map(OwnerMapper::toDomain);
    }
}
