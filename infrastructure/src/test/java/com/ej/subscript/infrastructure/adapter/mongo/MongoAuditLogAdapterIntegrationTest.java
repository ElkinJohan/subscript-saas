package com.ej.subscript.infrastructure.adapter.mongo;

import com.ej.subscript.domain.audit.AuditEvent;
import com.ej.subscript.domain.audit.AuditEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataMongoTest
@Import(MongoAuditLogAdapter.class)
class MongoAuditLogAdapterIntegrationTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
    }

    @Autowired
    MongoAuditLogAdapter adapter;

    @Autowired
    AuditEventMongoRepository repository;

    @BeforeEach
    void cleanUp() {
        repository.deleteAll().block();
    }

    @Test
    void shouldPersistAuditEvent() {
        UUID ownerId = UUID.randomUUID();
        AuditEvent event = AuditEvent.of(
                AuditEventType.AUTH_LOGIN_SUCCESS,
                ownerId,
                Map.of("email", "ana@gym.com")
        );

        StepVerifier.create(adapter.record(event).then(repository.findById(event.id())))
                .assertNext(doc -> {
                    assertThat(doc.getType()).isEqualTo("AUTH_LOGIN_SUCCESS");
                    assertThat(doc.getOwnerId()).isEqualTo(ownerId);
                    assertThat(doc.getData()).containsEntry("email", "ana@gym.com");
                })
                .verifyComplete();
    }

    @Test
    void shouldNotEmitErrorWhenWriteSucceeds() {
        AuditEvent event = AuditEvent.of(AuditEventType.AUTH_LOGOUT, UUID.randomUUID(), Map.of());

        StepVerifier.create(adapter.record(event))
                .verifyComplete();
    }
}
