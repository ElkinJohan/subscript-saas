package com.ej.subscript.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Token lifetime configuration bound from {@code security.token.*} in
 * {@code application.yml}.
 *
 * <p>Both fields accept Spring's relaxed duration syntax (e.g. {@code 15m},
 * {@code 7d}, {@code 2h}, {@code 30s}). The defaults live in
 * {@code application.yml} as placeholder fallbacks
 * ({@code ${ACCESS_TOKEN_TTL:15m}}), making the YAML the single source
 * of truth — env vars override per environment.
 *
 * <p>If the property is missing from the YAML the bind will fail loudly
 * at boot, which is the desired behavior: silent defaults from two
 * sources are how config drift starts.
 */
@ConfigurationProperties(prefix = "security.token")
public record SecurityTokenProperties(
        Duration accessTtl,
        Duration refreshTtl
) {
}
