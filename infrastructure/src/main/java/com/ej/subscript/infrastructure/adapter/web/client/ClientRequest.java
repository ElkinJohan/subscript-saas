package com.ej.subscript.infrastructure.adapter.web.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del request {@code POST /api/owners/{ownerId}/clients}.
 *
 * <p>El {@code ownerId} no viaja en el body — se toma del path en el handler.
 * Eso evita ambigüedad sobre quién es el padre del Client y simplifica futuras
 * reglas de autorización a nivel de fila.
 *
 * @param cedula identificación del cliente (cédula/tax id). Obligatoria y no en blanco.
 * @param name   nombre del cliente. Obligatorio y no en blanco.
 * @param email  correo del cliente. Obligatorio y con formato válido.
 * @param phone  teléfono opcional, formato libre.
 */
public record ClientRequest(
        @NotBlank String cedula,
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone
) {
}
