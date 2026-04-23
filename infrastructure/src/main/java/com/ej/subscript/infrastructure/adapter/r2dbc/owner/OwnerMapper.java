package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import com.ej.subscript.domain.model.Owner;

class OwnerMapper {

    static OwnerEntity toEntity(Owner owner) {
        return new OwnerEntity(
                owner.id(), owner.nit(), owner.name(), owner.email(),
                owner.phone(), owner.businessName(), owner.gracePeriodDays(), true
        );
    }

    static Owner toDomain(OwnerEntity entity) {
        return new Owner(
                entity.getId(), entity.getNit(), entity.getName(), entity.getEmail(),
                entity.getPhone(), entity.getBusinessName(), entity.getGracePeriodDays()
        );
    }
}
