package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.domain.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Outbound adapter (Hexagonal Architecture) that implements
 * {@link OwnerRepository} via Spring Data R2DBC. Translates between the
 * domain model ({@link Owner}) and the persistence entity
 * ({@link OwnerEntity}) using {@link OwnerMapper}.
 */
@Repository
@RequiredArgsConstructor
public class OwnerRepositoryAdapter implements OwnerRepository {

    private final OwnerR2dbcRepository r2dbcRepository;

    @Override
    public Mono<Owner> save(Owner owner) {
        return r2dbcRepository.save(OwnerMapper.toEntity(owner))
                .map(OwnerMapper::toDomain);
    }

    @Override
    public Mono<Owner> findById(String id) {
        return r2dbcRepository.findById(java.util.UUID.fromString(id))
                .map(OwnerMapper::toDomain);
    }

    @Override
    public Mono<Owner> findByEmail(String email) {
        return r2dbcRepository.findByEmail(email)
                .map(OwnerMapper::toDomain);
    }

    @Override
    public Mono<Owner> findByNit(String nit) {
        return r2dbcRepository.findByNit(nit)
                .map(OwnerMapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return r2dbcRepository.deleteById(java.util.UUID.fromString(id));
    }
}
