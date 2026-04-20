package com.ej.subscript.domain.model;

import java.util.UUID;

/**
 * Representa al dueño de una suscripción en el sistema.
 * Es un Record: inmutable, con equals, hashCode y toString automáticos.
 */
public record Owner(
        UUID id,
        String name,
        String email,
        String businessName
) {
    // Aquí podrías añadir validaciones compactas si fuera necesario
    public Owner {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
    }
}