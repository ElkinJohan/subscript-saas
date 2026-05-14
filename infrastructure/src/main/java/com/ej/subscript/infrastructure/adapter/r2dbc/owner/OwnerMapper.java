package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import com.ej.subscript.domain.model.Owner;

/**
 * Translator between the domain {@link Owner} record and the R2DBC
 * {@link OwnerEntity}. Keeping it in a separate type keeps the entity
 * and the domain record decoupled: neither knows about the other.
 */
class OwnerMapper {

    /**
     * To persistence. Marks {@code isNew = true} so Spring Data fires an
     * INSERT — see {@link OwnerEntity} for the rationale.
     */
    static OwnerEntity toEntity(Owner owner) {
        OwnerEntity e = new OwnerEntity(
                owner.id(), owner.nit(), owner.name(), owner.email(),
                owner.phone(), owner.businessName(), owner.gracePeriodDays(),
                owner.passwordHash(), true
        );
        return e;
    }

    /**
     * To domain. The record's compact constructor validates invariants, so
     * a corrupted row in the database is rejected at hydration — it never
     * reaches the application layer in an invalid state.
     */
    static Owner toDomain(OwnerEntity entity) {
        return new Owner(
                entity.getId(), entity.getNit(), entity.getName(), entity.getEmail(),
                entity.getPhone(), entity.getBusinessName(), entity.getGracePeriodDays(),
                entity.getPasswordHash()
        );
    }
}
