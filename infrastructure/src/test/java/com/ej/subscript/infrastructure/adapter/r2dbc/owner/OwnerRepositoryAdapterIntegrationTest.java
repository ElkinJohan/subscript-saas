package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import com.ej.subscript.domain.model.Owner;
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
@Import(OwnerRepositoryAdapter.class)
class OwnerRepositoryAdapterIntegrationTest {

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
    OwnerRepositoryAdapter adapter;

    @Autowired
    OwnerR2dbcRepository r2dbcRepository;

    @BeforeEach
    void cleanUp() {
        r2dbcRepository.deleteAll().block();
    }

    @Test
    void shouldPersistAndRetrieveOwnerById() {
        Owner owner = new Owner(
                UUID.randomUUID(), "900111", "Ana", "ana@gym.com",
                "300", "AnaFit", 5, "$2a$10$hash"
        );

        StepVerifier.create(adapter.save(owner).then(adapter.findById(owner.id().toString())))
                .assertNext(found -> {
                    assertThat(found.id()).isEqualTo(owner.id());
                    assertThat(found.email()).isEqualTo("ana@gym.com");
                    assertThat(found.passwordHash()).isEqualTo("$2a$10$hash");
                })
                .verifyComplete();
    }

    @Test
    void shouldFindOwnerByEmail() {
        Owner owner = new Owner(
                UUID.randomUUID(), "900222", "Bruno", "bruno@gym.com",
                "301", "BrunoFit", 3, "$2a$10$hash"
        );

        StepVerifier.create(adapter.save(owner).then(adapter.findByEmail("bruno@gym.com")))
                .assertNext(found -> assertThat(found.nit()).isEqualTo("900222"))
                .verifyComplete();
    }

    @Test
    void shouldEmitEmptyWhenIdDoesNotExist() {
        StepVerifier.create(adapter.findById(UUID.randomUUID().toString()))
                .verifyComplete();
    }

    @Test
    void shouldRejectDuplicateEmailAtDatabaseLevel() {
        Owner first = new Owner(
                UUID.randomUUID(), "900333", "Carla", "carla@gym.com",
                "302", "CarlaFit", 0, "$2a$10$hash"
        );
        Owner duplicate = new Owner(
                UUID.randomUUID(), "900444", "Diego", "carla@gym.com",
                "303", "DiegoFit", 0, "$2a$10$hash"
        );

        StepVerifier.create(adapter.save(first).then(adapter.save(duplicate)))
                .expectError()
                .verify();
    }

    @Test
    void shouldFindOwnerByNit() {
        Owner owner = new Owner(
                UUID.randomUUID(), "900555", "Elena", "elena@gym.com",
                "304", "ElenaFit", 7, "$2a$10$hash"
        );

        StepVerifier.create(adapter.save(owner).then(adapter.findByNit("900555")))
                .assertNext(found -> assertThat(found.email()).isEqualTo("elena@gym.com"))
                .verifyComplete();
    }

    @Test
    void shouldRejectDuplicateNitAtDatabaseLevel() {
        Owner first = new Owner(
                UUID.randomUUID(), "900666", "Felipe", "felipe@gym.com",
                "305", "FelipeFit", 0, "$2a$10$hash"
        );
        Owner duplicate = new Owner(
                UUID.randomUUID(), "900666", "Gabriela", "gaby@gym.com",
                "306", "GabyFit", 0, "$2a$10$hash"
        );

        StepVerifier.create(adapter.save(first).then(adapter.save(duplicate)))
                .expectError()
                .verify();
    }
}
