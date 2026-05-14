package com.ej.subscript.infrastructure.security;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * List of revoked tokens. Allows invalidating JWTs before their natural
 * expiration (explicit logout, refresh-token rotation, compromised session).
 *
 * <p>The identifier is the {@code jti} (JWT ID) claim, a UUID minted by
 * {@link JwtService}. Each entry has a TTL equal to the token's remaining
 * lifetime — Redis evicts it automatically once expired.
 */
public interface TokenBlacklist {

    /**
     * Marks a {@code jti} as revoked for {@code ttl}.
     */
    Mono<Void> blacklist(String jti, Duration ttl);

    /**
     * {@code true} if the {@code jti} has been revoked and its TTL has not
     * expired yet.
     */
    Mono<Boolean> isBlacklisted(String jti);
}
