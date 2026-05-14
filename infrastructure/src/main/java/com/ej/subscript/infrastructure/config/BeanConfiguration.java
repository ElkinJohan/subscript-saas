package com.ej.subscript.infrastructure.config;

import com.ej.subscript.application.usecase.*;
import com.ej.subscript.domain.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra manualmente los use cases como beans de Spring.
 *
 * <p>Los use cases son clases Java puras (sin {@code @Component}) para mantener
 * la capa de aplicación desacoplada del framework. Este archivo es el único punto
 * donde Spring "conoce" los use cases — si algún día se migra de Spring, solo
 * hay que cambiar este archivo, no la lógica de negocio.
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
