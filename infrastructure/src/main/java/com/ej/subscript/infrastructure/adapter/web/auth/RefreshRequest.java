package com.ej.subscript.infrastructure.adapter.web.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo del request {@code POST /api/auth/refresh} y {@code POST /api/auth/logout}.
 *
 * @param refreshToken JWT de refresh emitido previamente por la API. Obligatorio y no en blanco.
 *                     En {@code /refresh} actúa como credencial — su {@code jti} se blacklistea
 *                     antes de emitir el par nuevo. En {@code /logout} se blacklistea junto al
 *                     access token del header.
 */
public record RefreshRequest(@NotBlank String refreshToken) {
}
