package com.ej.subscript.infrastructure.config;

import com.ej.subscript.application.usecase.*;
import com.ej.subscript.domain.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public OwnerUseCase ownerUseCase(OwnerRepository ownerRepository) {
        return new OwnerUseCase(ownerRepository);
    }

    @Bean
    public ClientUseCase clientUseCase(ClientRepository clientRepository) {
        return new ClientUseCase(clientRepository);
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
