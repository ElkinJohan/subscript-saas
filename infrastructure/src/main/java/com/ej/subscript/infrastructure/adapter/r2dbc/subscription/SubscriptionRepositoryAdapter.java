package com.ej.subscript.infrastructure.adapter.r2dbc.subscription;

import com.ej.subscript.domain.model.Subscription;
import com.ej.subscript.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adaptador de salida que implementa {@link SubscriptionRepository} con R2DBC.
 * Ver {@code ClientRepositoryAdapter} para la explicación del patrón save/update con {@code Persistable}.
 */
@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {

    private final SubscriptionR2dbcRepository r2dbcRepository;

    @Override
    public Mono<Subscription> save(Subscription subscription) {
        return r2dbcRepository.save(SubscriptionMapper.toEntity(subscription))
                .map(SubscriptionMapper::toDomain);
    }

    @Override
    public Mono<Subscription> update(Subscription subscription) {
        return r2dbcRepository.save(SubscriptionMapper.toEntityForUpdate(subscription))
                .map(SubscriptionMapper::toDomain);
    }

    @Override
    public Mono<Subscription> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(SubscriptionMapper::toDomain);
    }

    @Override
    public Flux<Subscription> findByClientId(UUID clientId) {
        return r2dbcRepository.findByClientId(clientId)
                .map(SubscriptionMapper::toDomain);
    }

    @Override
    public Mono<Subscription> findActiveByClientId(UUID clientId) {
        return r2dbcRepository.findActiveByClientId(clientId)
                .map(SubscriptionMapper::toDomain);
    }
}
