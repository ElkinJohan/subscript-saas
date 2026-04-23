package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.model.ClientStatus;

class ClientMapper {

    static ClientEntity toEntity(Client client) {
        return new ClientEntity(
                client.id(), client.ownerId(), client.cedula(), client.name(),
                client.email(), client.phone(), client.status().name()
        );
    }

    static Client toDomain(ClientEntity entity) {
        return new Client(
                entity.id(), entity.ownerId(), entity.cedula(), entity.name(),
                entity.email(), entity.phone(), ClientStatus.valueOf(entity.status())
        );
    }
}
