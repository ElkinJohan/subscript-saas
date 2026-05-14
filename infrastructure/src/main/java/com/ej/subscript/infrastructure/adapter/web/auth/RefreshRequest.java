package com.ej.subscript.infrastructure.adapter.web.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for {@code POST /api/auth/refresh} and {@code POST /api/auth/logout}.
 *
 * @param refreshToken refresh JWT previously issued by the API. Required
 *                     and non-blank. On {@code /refresh} it acts as the
 *                     credential — its {@code jti} is blacklisted before
 *                     the new pair is minted. On {@code /logout} it is
 *                     blacklisted together with the header's access token.
 */
public record RefreshRequest(@NotBlank String refreshToken) {
}
