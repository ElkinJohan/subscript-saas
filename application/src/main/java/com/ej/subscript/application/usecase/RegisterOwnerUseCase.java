package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.domain.repository.OwnerRepository;
import reactor.core.publisher.Mono;

/**
 * Caso de Uso: Registrar un nuevo dueño.
 * Aplica la Inversión de Dependencia al recibir la interfaz del repositorio.
 */
public class RegisterOwnerUseCase {

    private final OwnerRepository repository;

    public RegisterOwnerUseCase(OwnerRepository repository) {
        this.repository = repository;
    }

    public Mono<Owner> execute(Owner owner) {
        // Aquí podrías agregar lógica de negocio adicional (ej. verificar si el email ya existe)
        return repository.save(owner);
    }
}