package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.domain.repository.OwnerRepository;
import reactor.core.publisher.Mono;

public class OwnerUseCase {

    private final OwnerRepository ownerRepository;

    public OwnerUseCase(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    public Mono<Owner> register(Owner owner) {
        return ownerRepository.findByEmail(owner.email())
                .flatMap(existing -> Mono.<Owner>error(new BusinessException(
                        "Email ya registrado", 409,
                        "Ya existe un owner con el email " + owner.email())))
                .switchIfEmpty(ownerRepository.save(owner));
    }

    public Mono<Owner> findById(String id) {
        return ownerRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Owner no encontrado", 404, "No existe un owner con ID " + id)));
    }
}
