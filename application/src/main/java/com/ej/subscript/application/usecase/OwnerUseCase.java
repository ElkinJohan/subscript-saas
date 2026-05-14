package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.domain.repository.OwnerRepository;
import reactor.core.publisher.Mono;

/**
 * Orquesta los casos de uso del Owner.
 * No contiene lógica de negocio propia — delega al dominio y al repositorio.
 */
public class OwnerUseCase {

    private final OwnerRepository ownerRepository;

    public OwnerUseCase(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    /**
     * Registra un nuevo Owner garantizando unicidad de email y NIT.
     * Si ambos están duplicados, el conflicto de email gana (se evalúa primero).
     * El UNIQUE constraint en la DB es defense-in-depth — el chequeo a nivel use case
     * existe para devolver un 409 limpio en vez de dejar que el constraint violation
     * emerja como un 500.
     */
    public Mono<Owner> register(Owner owner) {
        return ownerRepository.findByEmail(owner.email())
                .flatMap(existing -> Mono.<Owner>error(new BusinessException(
                        "Email ya registrado", 409,
                        "Ya existe un owner con el email " + owner.email())))
                .switchIfEmpty(
                        ownerRepository.findByNit(owner.nit())
                                .flatMap(existing -> Mono.<Owner>error(new BusinessException(
                                        "NIT ya registrado", 409,
                                        "Ya existe un owner con el NIT " + owner.nit())))
                                .switchIfEmpty(ownerRepository.save(owner))
                );
    }

    /**
     * Busca un Owner por ID o emite 404.
     */
    public Mono<Owner> findById(String id) {
        return ownerRepository.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Owner no encontrado", 404, "No existe un owner con ID " + id)));
    }

    /**
     * Busca un Owner por email o emite 401.
     * Utilizado exclusivamente en el flujo de autenticación JWT.
     */
    public Mono<Owner> findByEmail(String email) {
        return ownerRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Credenciales inválidas", 401, "Email o contraseña incorrectos")));
    }
}
