package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.repository.ClientRepository;
import com.ej.subscript.domain.repository.OwnerRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Orchestrates Client use cases.
 * Plain Java class — no Spring annotations — to keep the application layer
 * decoupled from the framework. The bean is registered manually in
 * {@code BeanConfiguration}.
 */
public class ClientUseCase {

    private final ClientRepository clientRepository;
    private final OwnerRepository ownerRepository;

    public ClientUseCase(ClientRepository clientRepository, OwnerRepository ownerRepository) {
        this.clientRepository = clientRepository;
        this.ownerRepository = ownerRepository;
    }

    /**
     * Registers a new client. Validations, in order:
     * <ol>
     *     <li>The owner exists (404 otherwise, to prevent the FK constraint
     *     from surfacing as a 500).</li>
     *     <li>The cedula is not already registered for that same owner
     *     (409). A cedula can repeat across different owners — the UNIQUE
     *     constraint is compound over {@code (owner_id, cedula)}.</li>
     * </ol>
     * The DB UNIQUE constraint is defense in depth: if two concurrent
     * requests both pass the prior check, the DB rejects the second INSERT.
     */
    public Mono<Client> register(Client client) {
        return ownerRepository.findById(client.ownerId().toString())
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Owner not found", 404,
                        "No owner found with id " + client.ownerId())))
                .flatMap(owner -> clientRepository.findByOwnerIdAndCedula(client.ownerId(), client.cedula())
                        .flatMap(existing -> Mono.<Client>error(new BusinessException(
                                "Cedula already registered", 409,
                                "A client with cedula " + client.cedula()
                                        + " is already registered for this owner")))
                        .switchIfEmpty(clientRepository.save(client)));
    }

    /**
     * Deactivates the client, or emits 404 when it does not exist.
     */
    public Mono<Client> deactivate(UUID clientId) {
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Client not found", 404,
                        "No client found with id " + clientId)))
                .map(Client::deactivate)
                .flatMap(clientRepository::update);
    }

    /**
     * Activates the client, or emits 404 when it does not exist.
     * Idempotent: an already-ACTIVE client stays ACTIVE.
     */
    public Mono<Client> activate(UUID clientId) {
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Client not found", 404,
                        "No client found with id " + clientId)))
                .map(Client::activate)
                .flatMap(clientRepository::update);
    }

    /**
     * Returns every client of the given owner.
     */
    public Flux<Client> findByOwnerId(UUID ownerId) {
        return clientRepository.findByOwnerId(ownerId);
    }
}
