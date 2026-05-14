package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.domain.repository.OwnerRepository;
import reactor.core.publisher.Mono;

/**
 * Orchestrates Owner use cases.
 * Holds no business logic of its own — it delegates to the domain model
 * and the repository port.
 */
public class OwnerUseCase {

    private final OwnerRepository ownerRepository;

    public OwnerUseCase(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * Registers a new Owner, enforcing email and NIT uniqueness.
     * If both are duplicated, the email conflict wins (it is checked first).
     * The DB UNIQUE constraint is defense in depth — the use-case-level
     * check exists to return a clean 409 instead of letting the constraint
     * violation surface as a 500.
     */
    public Mono<Owner> register(Owner owner) {
        return ownerRepository.findByEmail(owner.email())
                .flatMap(existing -> Mono.<Owner>error(new BusinessException(
                        "Email already registered", 409,
                        "An owner with email " + owner.email() + " is already registered")))
                .switchIfEmpty(
                        ownerRepository.findByNit(owner.nit())
                                .flatMap(existing -> Mono.<Owner>error(new BusinessException(
                                        "NIT already registered", 409,
                                        "An owner with NIT " + owner.nit() + " is already registered")))
                                .switchIfEmpty(ownerRepository.save(owner))
                );
    }

    /**
     * Looks up an Owner by id, or emits 404.
     */
    public Mono<Owner> findById(String id) {
        return ownerRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Owner not found", 404, "No owner found with id " + id)));
    }

    /**
     * Looks up an Owner by email, or emits 401.
     * Used exclusively by the JWT authentication flow.
     */
    public Mono<Owner> findByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Invalid credentials", 401, "Email or password are incorrect")));
    }
}
