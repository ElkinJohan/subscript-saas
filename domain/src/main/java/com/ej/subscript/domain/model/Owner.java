package com.ej.subscript.domain.model;

import java.util.UUID;

public record Owner(
        UUID id,
        String nit,
        String name,
        String email,
        String phone,
        String businessName,
        int gracePeriodDays
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
                               String phone, String businessName, int gracePeriodDays) {
        return new Owner(UUID.randomUUID(), nit, name, email, phone, businessName, gracePeriodDays);
    }
}
