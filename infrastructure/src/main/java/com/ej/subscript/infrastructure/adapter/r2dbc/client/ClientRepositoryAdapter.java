package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adaptador de salida que implementa {@link ClientRepository} con R2DBC.
 *
 * <p>{@code save} usa {@link ClientMapper#toEntity} ({@code isNew=true}) → R2DBC hace INSERT.
 * {@code update} usa {@link ClientMapper#toEntityForUpdate} ({@code isNew=false}) → R2DBC hace UPDATE.
 * La distinción es necesaria porque el UUID ya existe en el dominio antes de persistir,
 * y sin {@code Persistable.isNew()} R2DBC intentaría un UPDATE en un INSERT.
 */
@Repository
@RequiredArgsConstructor
public class ClientRepositoryAdapter implements ClientRepository {

    private final ClientR2dbcRepository r2dbcRepository;

    @Override
    public Mono<Client> save(Client client) {
        return r2dbcRepository.save(ClientMapper.toEntity(client))
                .map(ClientMapper::toDomain);
    }

    @Override
    public Mono<Client> update(Client client) {
        return r2dbcRepository.save(ClientMapper.toEntityForUpdate(client))
                .map(ClientMapper::toDomain);
    }

    @Override
    public Mono<Client> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(ClientMapper::toDomain);
    }

    @Override
    public Flux<Client> findByOwnerId(UUID ownerId) {
        return r2dbcRepository.findByOwnerId(ownerId)
                .map(ClientMapper::toDomain);
    }

    @Override
    public Mono<Client> findByOwnerIdAndCedula(UUID ownerId, String cedula) {
        return r2dbcRepository.findByOwnerIdAndCedula(ownerId, cedula)
                .map(ClientMapper::toDomain);
    }
}
