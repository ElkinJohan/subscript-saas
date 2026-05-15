package com.ej.subscript.infrastructure.adapter.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/auth/login}.
 *
 * @param email    Owner's email; required and must be a valid email.
 * @param password plaintext password; required and non-blank. It is
 *                 compared against the stored BCrypt hash — never
 *                 persisted nor logged.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
