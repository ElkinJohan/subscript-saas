package com.ej.subscript.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

/**
 * JWT decoder that rejects revoked tokens in addition to cryptographically
 * invalid ones.
 * <p>
 * Wraps a {@link ReactiveJwtDecoder}, delegating signature validation to it
 * and adding an extra step: looking up the {@code jti} in the
 * {@link TokenBlacklist}. If the token is revoked it emits a
 * {@link RevokedTokenException}, which produces a 401 both at the Spring
 * Security filter and at the refresh endpoint, letting application handlers
 * distinguish a revoked-token reuse from other JWT errors (invalid
 * signature, expired, etc.).
 */
@RequiredArgsConstructor
public class BlacklistAwareJwtDecoder implements ReactiveJwtDecoder {

    private final ReactiveJwtDecoder delegate;
    private final TokenBlacklist blacklist;

    @Override
    public Mono<Jwt> decode(String token) {
        return delegate.decode(token)
                .flatMap(jwt -> blacklist.isBlacklisted(jwt.getId())
                        .flatMap(revoked -> revoked
                                ? Mono.error(new RevokedTokenException(jwt.getId(), jwt.getSubject()))
                                : Mono.just(jwt)));
    }
}
