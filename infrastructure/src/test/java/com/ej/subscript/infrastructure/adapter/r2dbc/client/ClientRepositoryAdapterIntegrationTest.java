package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.model.ClientStatus;
import com.ej.subscript.infrastructure.adapter.r2dbc.owner.OwnerEntity;
import com.ej.subscript.infrastructure.adapter.r2dbc.owner.OwnerR2dbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataR2dbcTest
@Import(ClientRepositoryAdapter.class)
class ClientRepositoryAdapterIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("subscript")
            .withUsername("subscript")
            .withPassword("subscript");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://%s:%d/%s".formatted(
                POSTGRES.getHost(), POSTGRES.getFirstMappedPort(), POSTGRES.getDatabaseName()));
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
        registry.add("spring.sql.init.mode", () -> "always");
        registry.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");
    }

    @Autowired
    ClientRepositoryAdapter adapter;

    @Autowired
    ClientR2dbcRepository clientR2dbcRepository;

    @Autowired
    OwnerR2dbcRepository ownerR2dbcRepository;

    @BeforeEach
    void cleanUp() {
        clientR2dbcRepository.deleteAll()
                .then(ownerR2dbcRepository.deleteAll())
                .block();
    }

    /**
     * Inserta un Owner directamente vía R2DBC (sin pasar por OwnerRepositoryAdapter)
     * para satisfacer la FK constraint antes de insertar clientes en cada test.
     * {@code isNew = true} obliga a Spring Data a emitir INSERT (mismo patrón
     * que usa OwnerMapper.toEntity).
     */
    private UUID insertOwnerFixture(String nit, String email) {
        UUID ownerId = UUID.randomUUID();
        ownerR2dbcRepository.save(new OwnerEntity(
                ownerId, nit, "Owner Test", email, "300", "Owner Test SA", 0, "$2a$10$hash", true
        )).block();
        return ownerId;
    }

    @Test
    void shouldFindClientByOwnerIdAndCedula() {
        UUID ownerId = insertOwnerFixture("900111", "owner1@test.com");
        Client client = new Client(
                UUID.randomUUID(), ownerId, "CC12345", "Carlos",
                "carlos@test.com", "300", ClientStatus.ACTIVE
        );

        StepVerifier.create(adapter.save(client)
                        .then(adapter.findByOwnerIdAndCedula(ownerId, "CC12345")))
                .assertNext(found -> {
                    assertThat(found.id()).isEqualTo(client.id());
                    assertThat(found.cedula()).isEqualTo("CC12345");
                })
                .verifyComplete();
    }

    @Test
    void shouldRejectDuplicateCedulaForSameOwnerAtDatabaseLevel() {
        UUID ownerId = insertOwnerFixture("900222", "owner2@test.com");
        Client first = new Client(
                UUID.randomUUID(), ownerId, "CC99999", "Bruno",
                "bruno@test.com", "301", ClientStatus.ACTIVE
        );
        Client duplicate = new Client(
                UUID.randomUUID(), ownerId, "CC99999", "Bruna",
                "bruna@test.com", "302", ClientStatus.ACTIVE
        );

        StepVerifier.create(adapter.save(first).then(adapter.save(duplicate)))
                .expectError()
                .verify();
    }

    @Test
    void shouldAllowSameCedulaAcrossDifferentOwners() {
        UUID ownerA = insertOwnerFixture("900333", "ownerA@test.com");
        UUID ownerB = insertOwnerFixture("900444", "ownerB@test.com");
        Client clientForA = new Client(
                UUID.randomUUID(), ownerA, "CC55555", "Carlos para A",
                "carlosA@test.com", "303", ClientStatus.ACTIVE
        );
        Client clientForB = new Client(
                UUID.randomUUID(), ownerB, "CC55555", "Carlos para B",
                "carlosB@test.com", "304", ClientStatus.ACTIVE
        );

        StepVerifier.create(adapter.save(clientForA).then(adapter.save(clientForB)))
                .assertNext(saved -> {
                    assertThat(saved.cedula()).isEqualTo("CC55555");
                    assertThat(saved.ownerId()).isEqualTo(ownerB);
                })
                .verifyComplete();
    }
}
