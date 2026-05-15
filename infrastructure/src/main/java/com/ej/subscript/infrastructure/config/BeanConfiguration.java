package com.ej.subscript.infrastructure.config;

import com.ej.subscript.application.usecase.*;
import com.ej.subscript.domain.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Manually registers the use cases as Spring beans.
 *
 * <p>The use cases are plain Java classes (no {@code @Component}) so the
 * application layer stays decoupled from the framework. This file is the
 * only place where Spring "knows" about the use cases — if Spring is ever
 * swapped for another container, only this file needs to change, not the
 * business logic.
 */
@Configuration
public class BeanConfiguration {

    @Bean
    public OwnerUseCase ownerUseCase(OwnerRepository ownerRepository) {
        return new OwnerUseCase(ownerRepository);
    }

    @Bean
    public ClientUseCase clientUseCase(ClientRepository clientRepository,
                                       OwnerRepository ownerRepository) {
        return new ClientUseCase(clientRepository, ownerRepository);
    }

    @Bean
    public PlanUseCase planUseCase(PlanRepository planRepository) {
        return new PlanUseCase(planRepository);
    }

    @Bean
    public SubscriptionUseCase subscriptionUseCase(SubscriptionRepository subscriptionRepository,
                                                   ClientRepository clientRepository,
                                                   PlanRepository planRepository) {
        return new SubscriptionUseCase(subscriptionRepository, clientRepository, planRepository);
    }

    @Bean
    public PaymentUseCase paymentUseCase(SubscriptionRepository subscriptionRepository,
                                         PaymentRepository paymentRepository) {
        return new PaymentUseCase(subscriptionRepository, paymentRepository);
    }
}
