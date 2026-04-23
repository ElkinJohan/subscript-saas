package com.ej.subscript.infrastructure.adapter.web.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank String cedula,
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone
) {}
