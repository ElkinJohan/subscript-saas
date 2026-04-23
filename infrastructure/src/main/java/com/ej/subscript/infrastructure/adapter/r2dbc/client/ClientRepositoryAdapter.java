package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.repository.ClientRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class ClientRepositoryAdapter implements ClientRepository {

    private final ClientR2dbcRepository r2dbcRepository;

    public ClientRepositoryAdapter(ClientR2dbcRepository r2dbcRepository) {
        this.r2dbcRepository = r2dbcRepository;
    }

    @Override
    public Mono<Client> save(Client client) {
        return r2dbcRepository.save(ClientMapper.toEntity(client))
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
}
