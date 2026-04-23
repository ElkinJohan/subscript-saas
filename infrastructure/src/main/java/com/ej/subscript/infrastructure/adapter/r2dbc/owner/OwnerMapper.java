package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import com.ej.subscript.domain.model.Owner;

class OwnerMapper {

    static OwnerEntity toEntity(Owner owner) {
        return new OwnerEntity(
                owner.id(), owner.nit(), owner.name(), owner.email(),
                owner.phone(), owner.businessName(), owner.gracePeriodDays()
        );
    }

    static Owner toDomain(OwnerEntity entity) {
        return new Owner(
                entity.id(), entity.nit(), entity.name(), entity.email(),
                entity.phone(), entity.businessName(), entity.gracePeriodDays()
        );
    }
}
