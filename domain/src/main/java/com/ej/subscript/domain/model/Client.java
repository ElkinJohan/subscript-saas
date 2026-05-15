package com.ej.subscript.domain.model;

import com.ej.subscript.domain.exception.BusinessException;

import java.util.UUID;

/**
 * Client registered by an Owner on the platform.
 * Represents the person who buys and keeps subscriptions
 * (e.g. a gym member, a barber-shop customer).
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
    /**
     * Domain invariants checked on every construction path. Mirroring the
     * request-level validators here keeps the model from existing in an
     * invalid state — including when hydrating from the database via
     * {@link com.ej.subscript.infrastructure.adapter.r2dbc.client.ClientMapper}.
     *
     * @throws BusinessException 422 when any required field is null/blank.
     */
    public Client {
        if (ownerId == null)
            throw new BusinessException("Invalid input", 422, "OwnerId is required");
        if (cedula == null || cedula.isBlank())
            throw new BusinessException("Invalid input", 422, "Cedula is required");
        if (name == null || name.isBlank())
            throw new BusinessException("Invalid input", 422, "Name is required");
        if (email == null || email.isBlank())
            throw new BusinessException("Invalid input", 422, "Email is required");
        if (status == null)
            throw new BusinessException("Invalid input", 422, "Status is required");
    }

    /**
     * Creates a new client in ACTIVE state.
     */
    public static Client create(UUID ownerId, String cedula, String name, String email, String phone) {
        return new Client(UUID.randomUUID(), ownerId, cedula, name, email, phone, ClientStatus.ACTIVE);
    }

    /**
     * Returns a new instance in INACTIVE state. Does not mutate the receiver.
     */
    public Client deactivate() {
        return new Client(id, ownerId, cedula, name, email, phone, ClientStatus.INACTIVE);
    }

    /**
     * Returns a new instance in ACTIVE state.
     */
    public Client activate() {
        return new Client(id, ownerId, cedula, name, email, phone, ClientStatus.ACTIVE);
    }
}
