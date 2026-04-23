package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.model.ClientStatus;

class ClientMapper {

    static ClientEntity toEntity(Client client) {
        return new ClientEntity(
                client.id(), client.ownerId(), client.cedula(), client.name(),
                client.email(), client.phone(), client.status().name(), true
        );
    }

    static Client toDomain(ClientEntity entity) {
        return new Client(
                entity.getId(), entity.getOwnerId(), entity.getCedula(), entity.getName(),
                entity.getEmail(), entity.getPhone(), ClientStatus.valueOf(entity.getStatus())
        );
    }
}
