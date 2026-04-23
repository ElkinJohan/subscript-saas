package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Client;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ClientRepository {
    Mono<Client> save(Client client);
    Mono<Client> update(Client client);
    Mono<Client> findById(UUID id);
    Flux<Client> findByOwnerId(UUID ownerId);
}
