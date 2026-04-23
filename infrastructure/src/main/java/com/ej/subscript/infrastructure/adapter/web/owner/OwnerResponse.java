package com.ej.subscript.infrastructure.adapter.web.owner;

import com.ej.subscript.domain.model.Owner;

import java.util.UUID;

public record OwnerResponse(
        UUID id,
        String nit,
        String name,
        String email,
        String phone,
        String businessName,
        int gracePeriodDays
) {
    static OwnerResponse from(Owner owner) {
        return new OwnerResponse(
                owner.id(), owner.nit(), owner.name(), owner.email(),
                owner.phone(), owner.businessName(), owner.gracePeriodDays()
        );
    }
}
