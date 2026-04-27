package com.ej.subscript.infrastructure.adapter.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface AuditEventMongoRepository extends ReactiveMongoRepository<AuditEventDocument, String> {
}
