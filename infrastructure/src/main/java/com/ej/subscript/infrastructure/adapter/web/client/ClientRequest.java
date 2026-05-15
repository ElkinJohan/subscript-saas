package com.ej.subscript.infrastructure.adapter.web.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/owners/{ownerId}/clients}.
 *
 * <p>The {@code ownerId} is taken from the path — not from the body — so
 * the parent of the Client is unambiguous and row-level authorization
 * rules are easier to reason about.
 *
 * @param cedula client identification (cedula / tax id). Required, non-blank.
 * @param name   client name. Required, non-blank.
 * @param email  client email. Required and well-formed.
 * @param phone  optional phone, free form.
 */
public record ClientRequest(
        @NotBlank String cedula,
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone
) {
}
