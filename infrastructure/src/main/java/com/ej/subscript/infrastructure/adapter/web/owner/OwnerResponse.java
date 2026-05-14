package com.ej.subscript.infrastructure.adapter.web.owner;

import com.ej.subscript.domain.model.Owner;

import java.util.UUID;

/**
 * Public view of the Owner returned by the API.
 *
 * <p>Mirrors {@link Owner} but <b>intentionally omits the password hash</b>:
 * the presentation layer never exposes cryptographic material, not even
 * the digest. Any new field added to the domain model has to be opted in
 * here explicitly, so filtering is a deliberate decision instead of an
 * oversight.
 */
public record OwnerResponse(
        UUID id,
        String nit,
        String name,
        String email,
        String phone,
        String businessName,
        int gracePeriodDays
) {
    /**
     * Projects a domain {@link Owner} into its presentation DTO.
     */
    static OwnerResponse from(Owner owner) {
        return new OwnerResponse(
                owner.id(), owner.nit(), owner.name(), owner.email(),
                owner.phone(), owner.businessName(), owner.gracePeriodDays()
        );
    }
}
