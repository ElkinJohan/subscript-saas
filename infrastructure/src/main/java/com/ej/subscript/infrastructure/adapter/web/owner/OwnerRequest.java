package com.ej.subscript.infrastructure.adapter.web.owner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo del request {@code POST /api/owners}.
 *
 * <p>La validación de schema (Bean Validation) corre antes de que se construya el
 * agregado de dominio: si una restricción falla el handler emite {@code 400}.
 * Las invariantes adicionales del dominio se aplican después en el compact
 * constructor de {@link com.ej.subscript.domain.model.Owner}.
 *
 * @param nit             NIT/Tax ID del negocio. Obligatorio y no en blanco.
 * @param name            nombre del owner. Obligatorio y no en blanco.
 * @param email           correo, login identifier. Obligatorio y con formato válido.
 * @param phone           teléfono opcional, formato libre.
 * @param businessName    nombre comercial opcional.
 * @param gracePeriodDays días de gracia que el owner concede a sus clientes
 *                        antes de suspenderles la suscripción. Debe ser {@code >= 0}.
 * @param password        contraseña en claro, mínimo 8 caracteres. Se hashea con
 *                        BCrypt en el handler antes de construir el {@code Owner}
 *                        — nunca se persiste ni se loggea.
 */
public record OwnerRequest(
        @NotBlank String nit,
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        String businessName,
        @Min(0) int gracePeriodDays,
        @NotBlank @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres") String password
) {
}
