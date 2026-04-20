package com.ej.subscript.infrastructure.config;

import com.ej.subscript.application.usecase.RegisterOwnerUseCase;
import com.ej.subscript.domain.repository.OwnerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    // Aquí le decimos a Spring: "Cuando alguien necesite el Caso de Uso,
    // usa el repositorio que ya conoces y crea una instancia manual".
    @Bean
    public RegisterOwnerUseCase registerOwnerUseCase(OwnerRepository repository) {
        return new RegisterOwnerUseCase(repository);
    }
}