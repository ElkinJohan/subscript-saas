package com.ej.subscript.infrastructure.adapter.web.owner;

import com.ej.subscript.domain.model.Owner;

import java.util.UUID;

/**
 * Vista pública del Owner devuelta por la API.
 *
 * <p>Refleja a {@link Owner} pero <b>omite intencionalmente el hash de
 * contraseña</b>: la presentación nunca expone material criptográfico, ni
 * siquiera el digest. Cualquier campo que se agregue al modelo de dominio
 * debe sumarse acá explícitamente para que su filtrado sea una decisión
 * consciente, no un olvido.
 */
public record OwnerResponse(
        UUID id,
        String nit,
        String name,
        String email,
        String phone,
        String businessName,
        int gracePeriodDays
) {
    /**
     * Proyecta un {@link Owner} de dominio a su DTO de presentación.
     */
    static OwnerResponse from(Owner owner) {
        return new OwnerResponse(
                owner.id(), owner.nit(), owner.name(), owner.email(),
                owner.phone(), owner.businessName(), owner.gracePeriodDays()
        );
    }
}
