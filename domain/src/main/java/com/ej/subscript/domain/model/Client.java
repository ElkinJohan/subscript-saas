package com.ej.subscript.domain.model;

import java.util.UUID;

/**
 * Cliente registrado por un Owner en la plataforma.
 * Representa a la persona que compra y mantiene suscripciones
 * (e.g., el socio de un gimnasio, el cliente de una barbería).
 */
public record Client(
        UUID id,
        UUID ownerId,
        String cedula,
        String name,
        String email,
        String phone,
        ClientStatus status
) {
    public Client {
        if (ownerId == null)
            throw new IllegalArgumentException("El ownerId es obligatorio");
        if (cedula == null || cedula.isBlank())
            throw new IllegalArgumentException("La cédula es obligatoria");
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email es obligatorio");
        if (status == null)
            throw new IllegalArgumentException("El estado es obligatorio");
    }

    /** Crea un nuevo cliente con estado ACTIVE. */
    public static Client create(UUID ownerId, String cedula, String name, String email, String phone) {
        return new Client(UUID.randomUUID(), ownerId, cedula, name, email, phone, ClientStatus.ACTIVE);
    }

    /** Retorna una nueva instancia con estado INACTIVE. No muta el receptor. */
    public Client deactivate() {
        return new Client(id, ownerId, cedula, name, email, phone, ClientStatus.INACTIVE);
    }

    /** Retorna una nueva instancia con estado ACTIVE. */
    public Client activate() {
        return new Client(id, ownerId, cedula, name, email, phone, ClientStatus.ACTIVE);
    }
}
