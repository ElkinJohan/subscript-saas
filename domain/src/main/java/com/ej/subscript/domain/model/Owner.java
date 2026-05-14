package com.ej.subscript.domain.model;

import com.ej.subscript.domain.exception.BusinessException;

import java.util.UUID;

/**
 * Representa al dueño del negocio que usa la plataforma.
 * El {@code passwordHash} es el resultado del algoritmo BCrypt aplicado sobre
 * la contraseña en texto plano ingresada durante el registro. El dominio
 * nunca almacena ni conoce la contraseña original.
 */
public record Owner(
        UUID id,
        String nit,
        String name,
        String email,
        String phone,
        String businessName,
        int gracePeriodDays,
        String passwordHash
) {
    /**
     * Defensive invariants checked on every construction path — including
     * deserialization from the persistence layer. Mirroring the request-level
     * validators here means an Owner cannot exist in memory in a state the
     * domain considers invalid, regardless of how it got built.
     *
     * @throws BusinessException 422 when nit, name or email is blank, or when
     *                           {@code gracePeriodDays} is negative.
     */
    public Owner {
        if (nit == null || nit.isBlank())
            throw new BusinessException("Datos inválidos", 422, "El NIT es obligatorio");
        if (name == null || name.isBlank())
            throw new BusinessException("Datos inválidos", 422, "El nombre es obligatorio");
        if (email == null || email.isBlank())
            throw new BusinessException("Datos inválidos", 422, "El email es obligatorio");
        if (gracePeriodDays < 0)
            throw new BusinessException("Datos inválidos", 422, "El período de gracia no puede ser negativo");
    }

    /**
     * Factory para owners recién registrados: genera un {@code id} nuevo y deja
     * que el compact constructor valide los datos. Usar este método en vez del
     * canonical constructor garantiza que dos registros nunca compartan id, aun
     * en condiciones de concurrencia.
     *
     * @param passwordHash hash BCrypt ya calculado por la capa de aplicación;
     *                     este método no realiza hashing.
     * @return nuevo {@link Owner} con id generado.
     */
    public static Owner create(String nit, String name, String email,
                               String phone, String businessName,
                               int gracePeriodDays, String passwordHash) {
        return new Owner(UUID.randomUUID(), nit, name, email, phone,
                businessName, gracePeriodDays, passwordHash);
    }
}
