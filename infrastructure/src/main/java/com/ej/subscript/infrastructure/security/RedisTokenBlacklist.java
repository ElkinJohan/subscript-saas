package com.ej.subscript.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementación del {@link TokenBlacklist} respaldada por Redis.
 * <p>
 * Cada {@code jti} se almacena con la clave {@code blacklist:<jti>} y TTL igual
 * al tiempo restante del token. Redis se encarga de la limpieza automática.
 */
@Component
@RequiredArgsConstructor
public class RedisTokenBlacklist implements TokenBlacklist {

    private static final String KEY_PREFIX = "blacklist:";
    private static final String VALUE = "1";

    private final ReactiveStringRedisTemplate redis;

    @Override
    public Mono<Void> blacklist(String jti, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            return Mono.empty();
        }
        return redis.opsForValue().set(KEY_PREFIX + jti, VALUE, ttl).then();
    }

    @Override
    public Mono<Boolean> isBlacklisted(String jti) {
        return redis.hasKey(KEY_PREFIX + jti);
    }
}
