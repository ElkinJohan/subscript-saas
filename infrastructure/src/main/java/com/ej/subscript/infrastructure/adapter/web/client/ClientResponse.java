package com.ej.subscript.infrastructure.adapter.web.client;

import com.ej.subscript.domain.model.Client;

import java.util.UUID;

/**
 * Presentation DTO for the Client.
 *
 * <p>Mirrors the domain model but projects {@code ClientStatus} as a
 * {@link String} to keep the API schema stable: if a new enum value is
 * added later, existing consumers keep reading a string and do not break
 * on deserialization of an unknown value.
 */
public record ClientResponse(
        UUID id,
        UUID ownerId,
        String cedula,
        String name,
        String email,
        String phone,
        String status
) {
    /**
     * Projects a domain {@link Client} into the presentation DTO,
     * converting the {@code status} enum to its name string.
     */
    static ClientResponse from(Client client) {
        return new ClientResponse(
                client.id(), client.ownerId(), client.cedula(), client.name(),
                client.email(), client.phone(), client.status().name()
        );
    }
}
