package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.model.ClientStatus;

/**
 * Traductor entre el modelo de dominio {@link Client} y la entidad R2DBC
 * {@link ClientEntity}.
 *
 * <p>Tiene dos rutas hacia la persistencia ({@link #toEntity} y
 * {@link #toEntityForUpdate}) en vez de una sola: la diferencia es el flag
 * {@code isNew} que controla INSERT vs UPDATE en Spring Data. Separar las
 * intenciones acá hace que el adapter exprese explícitamente qué operación
 * quiere, en vez de depender de heurísticas internas del driver.
 */
class ClientMapper {

    /**
     * Hacia la persistencia para INSERT. Marca {@code isNew = true} para que
     * Spring Data ignore la presencia del id y emita un INSERT.
     */
    static ClientEntity toEntity(Client client) {
        return new ClientEntity(
                client.id(), client.ownerId(), client.cedula(), client.name(),
                client.email(), client.phone(), client.status().name(), true
        );
    }

    /**
     * Hacia la persistencia para UPDATE. Marca {@code isNew = false} para que
     * Spring Data emita {@code UPDATE clients SET ... WHERE id = ?}.
     */
    static ClientEntity toEntityForUpdate(Client client) {
        return new ClientEntity(
                client.id(), client.ownerId(), client.cedula(), client.name(),
                client.email(), client.phone(), client.status().name(), false
        );
    }

    /**
     * Hacia el dominio. Convierte el {@code status} string de la DB al enum
     * {@link ClientStatus} y deja que el compact constructor del record
     * valide invariantes — una fila corrupta es rechazada al hidratar.
     */
    static Client toDomain(ClientEntity entity) {
        return new Client(
                entity.getId(), entity.getOwnerId(), entity.getCedula(), entity.getName(),
                entity.getEmail(), entity.getPhone(), ClientStatus.valueOf(entity.getStatus())
        );
    }
}
