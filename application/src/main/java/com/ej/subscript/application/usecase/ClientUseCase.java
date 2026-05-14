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
     * Registra un nuevo cliente. Valida que el owner exista antes de persistir;
     * de lo contrario emite 404 (en vez de dejar que la FK constraint rompa el INSERT
     * y emerja como 500). La cédula puede repetirse entre owners.
     */
    public Mono<Client> register(Client client) {
        return ownerRepository.findById(client.ownerId().toString())
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Owner no encontrado", 404,
                        "No existe un owner con ID " + client.ownerId())))
                .flatMap(owner -> clientRepository.save(client));
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
