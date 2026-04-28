package com.ej.subscript.infrastructure.adapter.web.owner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
