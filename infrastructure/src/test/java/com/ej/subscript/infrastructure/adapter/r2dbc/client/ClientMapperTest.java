package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.model.ClientStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ClientMapperTest {

    private static final Client CLIENT = new Client(
            UUID.randomUUID(), UUID.randomUUID(), "123456", "Carlos",
            "carlos@test.com", "300", ClientStatus.ACTIVE
    );

    @Test
    void shouldMapDomainToEntity() {
        ClientEntity entity = ClientMapper.toEntity(CLIENT);

        assertThat(entity.getId()).isEqualTo(CLIENT.id());
        assertThat(entity.getOwnerId()).isEqualTo(CLIENT.ownerId());
        assertThat(entity.getCedula()).isEqualTo(CLIENT.cedula());
        assertThat(entity.getName()).isEqualTo(CLIENT.name());
        assertThat(entity.getStatus()).isEqualTo(ClientStatus.ACTIVE.name());
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void shouldMapDomainToEntityForUpdate() {
        ClientEntity entity = ClientMapper.toEntityForUpdate(CLIENT);

        assertThat(entity.getId()).isEqualTo(CLIENT.id());
        assertThat(entity.isNew()).isFalse();
    }

    @Test
    void shouldMapEntityToDomain() {
        ClientEntity entity = ClientMapper.toEntity(CLIENT);
        Client result = ClientMapper.toDomain(entity);

        assertThat(result.id()).isEqualTo(CLIENT.id());
        assertThat(result.status()).isEqualTo(ClientStatus.ACTIVE);
    }

    @Test
    void shouldRoundTripWithoutDataLoss() {
        Client result = ClientMapper.toDomain(ClientMapper.toEntity(CLIENT));
        assertThat(result).isEqualTo(CLIENT);
    }
}
