package com.ej.subscript.domain.model;

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
    public Owner {
        if (nit == null || nit.isBlank())
            throw new IllegalArgumentException("El NIT es obligatorio");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio");
        if (gracePeriodDays < 0)
            throw new IllegalArgumentException("El período de gracia no puede ser negativo");
    }

    public static Owner create(String nit, String name, String email,
                               String phone, String businessName,
                               int gracePeriodDays, String passwordHash) {
        return new Owner(UUID.randomUUID(), nit, name, email, phone,
                businessName, gracePeriodDays, passwordHash);
    }
}
