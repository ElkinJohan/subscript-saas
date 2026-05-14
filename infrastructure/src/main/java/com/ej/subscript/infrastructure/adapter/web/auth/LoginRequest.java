package com.ej.subscript.infrastructure.adapter.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del request {@code POST /api/auth/login}.
 *
 * @param email    correo del Owner; obligatorio y con formato de email válido.
 * @param password contraseña en claro; obligatoria y no en blanco. Se compara
 *                 contra el hash BCrypt almacenado — nunca se persiste ni se loggea.
 */
public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
