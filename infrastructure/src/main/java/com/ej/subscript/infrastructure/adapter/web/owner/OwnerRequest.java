package com.ej.subscript.infrastructure.adapter.web.owner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OwnerRequest(
        @NotBlank String nit,
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        String businessName,
        @Min(0) int gracePeriodDays
) {}
