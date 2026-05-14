package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.repository.ClientRepository;
import com.ej.subscript.domain.repository.OwnerRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Orquesta los casos de uso del Client.
 * Clase Java pura — sin anotaciones de Spring — para mantener la capa de aplicación
 * independiente del framework. El bean se registra manualmente en {@code BeanConfiguration}.
 */
public class ClientUseCase {

    private final ClientRepository clientRepository;
    private final OwnerRepository ownerRepository;

    public ClientUseCase(ClientRepository clientRepository, OwnerRepository ownerRepository) {
        this.clientRepository = clientRepository;
        this.ownerRepository = ownerRepository;
    }

    /**
     * Registra un nuevo cliente. Validaciones en orden:
     * <ol>
     *     <li>El owner existe (404 en caso contrario, evita que la FK constraint emerja como 500).</li>
     *     <li>La cédula no está registrada para ese mismo owner (409). La cédula puede repetirse
     *     entre owners distintos — el UNIQUE constraint es compuesto sobre {@code (owner_id, cedula)}.</li>
     * </ol>
     * El UNIQUE constraint en la DB es defense-in-depth: si dos requests concurrentes pasan ambos
     * el check anterior, la DB rechaza el segundo INSERT.
     */
    public Mono<Client> register(Client client) {
        return ownerRepository.findById(client.ownerId().toString())
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Owner no encontrado", 404,
                        "No existe un owner con ID " + client.ownerId())))
                .flatMap(owner -> clientRepository.findByOwnerIdAndCedula(client.ownerId(), client.cedula())
                        .flatMap(existing -> Mono.<Client>error(new BusinessException(
                                "Cédula ya registrada", 409,
                                "Ya existe un cliente con la cédula " + client.cedula()
                                        + " para este owner")))
                        .switchIfEmpty(clientRepository.save(client)));
    }

    /**
     * Desactiva el cliente o emite 404 si no existe.
     */
    public Mono<Client> deactivate(UUID clientId) {
        return clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Cliente no encontrado", 404,
                        "No existe un cliente con ID " + clientId)))
                .map(Client::deactivate)
                .flatMap(clientRepository::update);
    }

    /**
     * Retorna todos los clientes del owner dado.
     */
    public Flux<Client> findByOwnerId(UUID ownerId) {
        return clientRepository.findByOwnerId(ownerId);
    }
}
