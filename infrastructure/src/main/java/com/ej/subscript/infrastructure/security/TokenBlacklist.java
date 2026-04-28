package com.ej.subscript.infrastructure.security;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Lista de tokens revocados. Permite invalidar JWTs antes de su expiración natural
 * (logout explícito, rotación de refresh tokens, sesión comprometida).
 *
 * <p>El identificador es el claim {@code jti} (JWT ID), un UUID emitido por
 * {@link JwtService}. Cada entrada tiene TTL igual al tiempo restante de vida
 * del token — Redis la elimina automáticamente al expirar.
 */
public interface TokenBlacklist {

    /**
     * Marca un {@code jti} como revocado durante {@code ttl}.
     */
    Mono<Void> blacklist(String jti, Duration ttl);

    /**
     * {@code true} si el {@code jti} fue revocado y aún no expiró su TTL.
     */
    Mono<Boolean> isBlacklisted(String jti);
}
