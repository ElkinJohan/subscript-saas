package com.ej.subscript.infrastructure.adapter.web.client;

import com.ej.subscript.domain.model.Client;

import java.util.UUID;

/**
 * DTO de presentación del Client.
 *
 * <p>Refleja el modelo de dominio pero proyecta el {@code ClientStatus} como
 * {@link String} para que la API tenga un schema estable: si en el futuro se
 * agrega un valor al enum, los consumidores existentes siguen leyendo un
 * string sin romperse, en lugar de fallar al deserializar un valor desconocido.
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
     * Proyecta un {@link Client} de dominio al DTO de presentación,
     * convirtiendo el enum {@code status} a su nombre string.
     */
    static ClientResponse from(Client client) {
        return new ClientResponse(
                client.id(), client.ownerId(), client.cedula(), client.name(),
                client.email(), client.phone(), client.status().name()
        );
    }
}
