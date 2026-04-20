package com.ej.subscript.infrastructure.adapter.rest;

import com.ej.subscript.application.usecase.RegisterOwnerUseCase;
import com.ej.subscript.domain.model.Owner;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final RegisterOwnerUseCase registerOwnerUseCase;

    public OwnerController(RegisterOwnerUseCase registerOwnerUseCase) {
        this.registerOwnerUseCase = registerOwnerUseCase;
    }

    @PostMapping
    public Mono<Owner> create(@RequestBody Owner owner) {
        // Retornamos un Mono<Owner>, Spring WebFlux se encarga de suscribirse
        // y enviar la respuesta cuando el dato esté listo sin bloquear el hilo.
        return registerOwnerUseCase.execute(owner);
    }
}