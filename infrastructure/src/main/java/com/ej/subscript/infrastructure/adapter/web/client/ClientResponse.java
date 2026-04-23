package com.ej.subscript.infrastructure.adapter.web.client;

import com.ej.subscript.domain.model.Client;

import java.util.UUID;

public record ClientResponse(
        UUID id,
        UUID ownerId,
        String cedula,
        String name,
        String email,
        String phone,
        String status
) {
    static ClientResponse from(Client client) {
        return new ClientResponse(
                client.id(), client.ownerId(), client.cedula(), client.name(),
                client.email(), client.phone(), client.status().name()
        );
    }
}
