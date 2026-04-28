package com.ej.subscript.infrastructure.adapter.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del request {@code POST /api/auth/login}.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
