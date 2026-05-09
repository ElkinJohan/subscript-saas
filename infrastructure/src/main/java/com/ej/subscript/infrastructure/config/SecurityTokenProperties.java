package com.ej.subscript.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Token lifetime configuration bound from {@code security.token.*} in
 * {@code application.yml}.
 *
 * <p>Both fields accept Spring's relaxed duration syntax (e.g. {@code 15m},
 * {@code 7d}, {@code 2h}, {@code 30s}) and fall back to OAuth2-friendly
 * defaults if the property is missing — 15 minutes for the access token,
 * 7 days for the refresh token.
 *
 * <p>The defaults match the values the codebase shipped with before this
 * was extracted into properties; profiles like {@code local} are expected
 * to override {@code accessTtl} for faster manual testing of the refresh
 * flow.
 */
@ConfigurationProperties(prefix = "security.token")
public record SecurityTokenProperties(
        @DefaultValue("15m") Duration accessTtl,
        @DefaultValue("7d") Duration refreshTtl
) {
}
