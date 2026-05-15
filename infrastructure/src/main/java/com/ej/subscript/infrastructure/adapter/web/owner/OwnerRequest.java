package com.ej.subscript.infrastructure.adapter.web.owner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for {@code POST /api/owners}.
 *
 * <p>Bean Validation runs before the domain aggregate is constructed: if a
 * constraint fails the handler emits {@code 400}. Additional domain
 * invariants run afterwards in the compact constructor of
 * {@link com.ej.subscript.domain.model.Owner}.
 *
 * @param nit             business NIT / tax id. Required, non-blank.
 * @param name            owner name. Required, non-blank.
 * @param email           login identifier. Required and well-formed.
 * @param phone           optional phone, free form.
 * @param businessName    optional commercial name.
 * @param gracePeriodDays grace period the owner grants to its clients
 *                        before suspending their subscriptions. Must be
 *                        {@code >= 0}.
 * @param password        plaintext password, at least 8 characters. Hashed
 *                        with BCrypt in the handler before constructing
 *                        the {@code Owner} — never persisted or logged.
 */
public record OwnerRequest(
        @NotBlank String nit,
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        String businessName,
        @Min(0) int gracePeriodDays,
        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long") String password
) {
}
