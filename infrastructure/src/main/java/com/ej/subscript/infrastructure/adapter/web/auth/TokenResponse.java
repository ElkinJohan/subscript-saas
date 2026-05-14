package com.ej.subscript.infrastructure.adapter.web.auth;

/**
 * Login endpoint response.
 *
 * @param accessToken  short-lived JWT (15 min). Sent on every request.
 * @param refreshToken long-lived JWT (7 days). Only used to renew the access token.
 */
public record TokenResponse(String accessToken, String refreshToken) {
}
