package com.ej.subscript.domain.repository;

import com.ej.subscript.domain.model.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Puerto de salida para la persistencia de {@link Subscription}.
 * Ver {@code ClientRepository} para la explicación del patrón save/update separados.
 */
public interface SubscriptionRepository {

    /**
     * Inserta una nueva Subscription (INSERT).
     */
    Mono<Subscription> save(Subscription subscription);

    /**
     * Actualiza una Subscription existente (UPDATE). Usado en cancel y renew.
     */
    Mono<Subscription> update(Subscription subscription);

    /**
     * Busca por ID; emite vacío si no existe.
     */
    Mono<Subscription> findById(UUID id);

    /**
     * Retorna todas las suscripciones del cliente dado, sin filtrar por estado.
     */
    Flux<Subscription> findByClientId(UUID clientId);

    /**
     * Retorna la suscripción con estado ACTIVE del cliente; emite vacío si no tiene ninguna.
     */
    Mono<Subscription> findActiveByClientId(UUID clientId);
}
