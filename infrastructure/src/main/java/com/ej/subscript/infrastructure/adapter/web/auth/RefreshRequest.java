package com.ej.subscript.infrastructure.adapter.web.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del request {@code POST /api/auth/refresh}.
 */
public record RefreshRequest(@NotBlank String refreshToken) {
}
