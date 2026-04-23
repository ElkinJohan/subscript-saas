package com.ej.subscript.infrastructure.adapter.web.auth;

/**
 * Respuesta del endpoint de login.
 *
 * @param accessToken  JWT de corta duración (15 min). Se envía en cada request.
 * @param refreshToken JWT de larga duración (7 días). Solo para renovar el access token.
 */
public record TokenResponse(String accessToken, String refreshToken) {}
