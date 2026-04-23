package com.ej.subscript.domain.model;

import java.util.UUID;

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

    public static Client create(UUID ownerId, String cedula, String name, String email, String phone) {
        return new Client(UUID.randomUUID(), ownerId, cedula, name, email, phone, ClientStatus.ACTIVE);
    }

    public Client deactivate() {
        return new Client(id, ownerId, cedula, name, email, phone, ClientStatus.INACTIVE);
    }

    public Client activate() {
        return new Client(id, ownerId, cedula, name, email, phone, ClientStatus.ACTIVE);
    }
}
