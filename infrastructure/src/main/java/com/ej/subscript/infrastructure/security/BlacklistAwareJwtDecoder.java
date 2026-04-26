package com.ej.subscript.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

/**
 * Decodificador de JWT que rechaza tokens revocados además de los inválidos.
 * <p>
 * Decora un {@link ReactiveJwtDecoder} delegándole la validación criptográfica
 * y agrega un paso adicional: consultar la {@link TokenBlacklist} por el {@code jti}.
 * Si el token está revocado se emite {@link BadJwtException}, lo que provoca un 401
 * tanto en el filtro de Spring Security como en el endpoint de refresh.
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
                                ? Mono.error(new BadJwtException("Token revocado"))
                                : Mono.just(jwt)));
    }
}
