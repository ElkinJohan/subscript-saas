package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.repository.ClientRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class ClientUseCase {

    private final ClientRepository clientRepository;

    public ClientUseCase(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Mono<Client> register(Client client) {
        return clientRepository.save(client);
    }

    public Mono<Client> deactivate(UUID clientId) {
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Cliente no encontrado", 404,
                        "No existe un cliente con ID " + clientId)))
                .map(Client::deactivate)
                .flatMap(clientRepository::update);
    }

    public Flux<Client> findByOwnerId(UUID ownerId) {
        return clientRepository.findByOwnerId(ownerId);
    }
}
