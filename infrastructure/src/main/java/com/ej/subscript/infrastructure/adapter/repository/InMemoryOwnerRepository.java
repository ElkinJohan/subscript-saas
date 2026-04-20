package com.ej.subscript.infrastructure.adapter.repository;

import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.domain.repository.OwnerRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOwnerRepository implements OwnerRepository {

    private final Map<String, Owner> database = new ConcurrentHashMap<>();

    @Override
    public Mono<Owner> save(Owner owner) {
        database.put(owner.id().toString(), owner);
        return Mono.just(owner);
    }

    @Override
    public Mono<Owner> findById(String id) {
        return Mono.justOrEmpty(database.get(id));
    }

    @Override
    public Mono<Owner> findByEmail(String email) {
        return Mono.justOrEmpty(database.values().stream()
                .filter(o -> o.email().equals(email))
                .findFirst());
    }
}