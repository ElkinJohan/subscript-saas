package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.PlanStatus;
import com.ej.subscript.domain.model.Subscription;
import com.ej.subscript.domain.model.SubscriptionStatus;
import com.ej.subscript.domain.repository.ClientRepository;
import com.ej.subscript.domain.repository.PlanRepository;
import com.ej.subscript.domain.repository.SubscriptionRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class SubscriptionUseCase {

    private final SubscriptionRepository subscriptionRepository;
    private final ClientRepository clientRepository;
    private final PlanRepository planRepository;

    public SubscriptionUseCase(SubscriptionRepository subscriptionRepository,
                               ClientRepository clientRepository,
                               PlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.clientRepository = clientRepository;
        this.planRepository = planRepository;
    }

    public Mono<Subscription> create(UUID clientId, UUID planId) {
        Mono<Void> clientExists = clientRepository.findById(clientId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Cliente no encontrado", 404,
                        "No existe un cliente con ID " + clientId)))
                .then();

        Mono<Subscription> subscription = planRepository.findById(planId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Plan no encontrado", 404,
                        "No existe un plan con ID " + planId)))
                .flatMap(plan -> {
                    if (plan.status() == PlanStatus.INACTIVE)
                        return Mono.error(new BusinessException(
                                "Plan inactivo", 422,
                                "El plan " + planId + " no está disponible para suscripción"));
                    return subscriptionRepository.save(
                            Subscription.create(clientId, planId, plan.price(), plan.durationDays()));
                });

        return clientExists.then(subscription);
    }

    public Mono<Subscription> cancel(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Suscripción no encontrada", 404,
                        "No existe una suscripción con ID " + subscriptionId)))
                .flatMap(sub -> {
                    if (sub.status() == SubscriptionStatus.CANCELLED)
                        return Mono.error(new BusinessException(
                                "Suscripción ya cancelada", 409,
                                "La suscripción " + subscriptionId + " ya fue cancelada"));
                    return subscriptionRepository.update(sub.cancel());
                });
    }

    public Mono<Subscription> renew(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Suscripción no encontrada", 404,
                        "No existe una suscripción con ID " + subscriptionId)))
                .flatMap(sub -> planRepository.findById(sub.planId())
                        .switchIfEmpty(Mono.error(new BusinessException(
                                "Plan no encontrado", 404,
                                "No existe un plan con ID " + sub.planId())))
                        .map(plan -> sub.renew(plan.durationDays())))
                .flatMap(subscriptionRepository::update);
    }

    public Flux<Subscription> findByClientId(UUID clientId) {
        return subscriptionRepository.findByClientId(clientId);
    }

    public Mono<Subscription> findActiveByClientId(UUID clientId) {
        return subscriptionRepository.findActiveByClientId(clientId)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Sin suscripción activa", 404,
                        "El cliente " + clientId + " no tiene suscripción activa")));
    }
}
