package com.ej.subscript.domain.model;

import com.ej.subscript.domain.exception.BusinessException;

import java.util.UUID;

/**
 * Represents the business owner using the platform.
 * The {@code passwordHash} is the result of applying BCrypt over the
 * plaintext password supplied at registration. The domain never stores
 * nor knows the original password.
 */
public record Owner(
        UUID id,
        String nit,
        String name,
        String email,
        String phone,
        String businessName,
        int gracePeriodDays,
        String passwordHash
) {
    /**
     * Defensive invariants checked on every construction path — including
     * deserialization from the persistence layer. Mirroring the request-level
     * validators here means an Owner cannot exist in memory in a state the
     * domain considers invalid, regardless of how it got built.
     *
     * @throws BusinessException 422 when nit, name or email is blank, or when
     *                           {@code gracePeriodDays} is negative.
     */
    public Owner {
        if (nit == null || nit.isBlank())
            throw new BusinessException("Invalid input", 422, "NIT is required");
        if (name == null || name.isBlank())
            throw new BusinessException("Invalid input", 422, "Name is required");
        if (email == null || email.isBlank())
            throw new BusinessException("Invalid input", 422, "Email is required");
        if (gracePeriodDays < 0)
            throw new BusinessException("Invalid input", 422, "Grace period cannot be negative");
    }

    /**
     * Factory for freshly registered owners: generates a new {@code id} and
     * lets the compact constructor validate the input. Using this method
     * instead of the canonical constructor guarantees that two records
     * never share an id, even under concurrent registrations.
     *
     * @param passwordHash BCrypt hash already computed by the application
     *                     layer; this method does not hash.
     * @return new {@link Owner} with a generated id.
     */
    public static Owner create(String nit, String name, String email,
                               String phone, String businessName,
                               int gracePeriodDays, String passwordHash) {
        return new Owner(UUID.randomUUID(), nit, name, email, phone,
                businessName, gracePeriodDays, passwordHash);
    }
}
