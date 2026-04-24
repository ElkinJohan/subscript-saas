package com.ej.subscript.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import com.ej.subscript.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClientTest {

    private static final UUID OWNER_ID = UUID.randomUUID();

    @Test
    void shouldCreateClientSuccessfully() {
        Client client = Client.create(OWNER_ID, "123456789", "Juan Pérez", "juan@email.com", "3001234567");

        assertThat(client.name()).isEqualTo("Juan Pérez");
        assertThat(client.cedula()).isEqualTo("123456789");
        assertThat(client.status()).isEqualTo(ClientStatus.ACTIVE);
    }

    @Test
    void shouldCreateWithFactory() {
        Client client = Client.create(OWNER_ID, "123456789", "Juan Pérez", "juan@email.com", null);

        assertThat(client.id()).isNotNull();
        assertThat(client.status()).isEqualTo(ClientStatus.ACTIVE);
        assertThat(client.phone()).isNull();
    }

    @Test
    void shouldDeactivateClient() {
        Client client = Client.create(OWNER_ID, "123456789", "Juan Pérez", "juan@email.com", null);

        Client deactivated = client.deactivate();

        assertThat(deactivated.status()).isEqualTo(ClientStatus.INACTIVE);
    }

    @Test
    void shouldActivateClient() {
        Client client = Client.create(OWNER_ID, "123456789", "Juan Pérez", "juan@email.com", null);
        Client deactivated = client.deactivate();

        Client activated = deactivated.activate();

        assertThat(activated.status()).isEqualTo(ClientStatus.ACTIVE);
    }

    @Test
    void shouldBeImmutableOnDeactivate() {
        Client client = Client.create(OWNER_ID, "123456789", "Juan Pérez", "juan@email.com", null);

        Client deactivated = client.deactivate();

        assertThat(deactivated).isNotSameAs(client);
        assertThat(client.status()).isEqualTo(ClientStatus.ACTIVE);
    }

    @Test
    void shouldThrowWhenOwnerIdIsNull() {
        assertThatThrownBy(() -> Client.create(null, "123456789", "Juan Pérez", "juan@email.com", null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenCedulaIsBlank() {
        assertThatThrownBy(() -> Client.create(OWNER_ID, "  ", "Juan Pérez", "juan@email.com", null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        assertThatThrownBy(() -> Client.create(OWNER_ID, "123456789", "  ", "juan@email.com", null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldThrowWhenEmailIsBlank() {
        assertThatThrownBy(() -> Client.create(OWNER_ID, "123456789", "Juan Pérez", "  ", null))
                .isInstanceOf(BusinessException.class);
    }
}
