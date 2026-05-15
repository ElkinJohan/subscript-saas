package com.ej.subscript.infrastructure.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Redis-backed implementation of {@link TokenBlacklist}.
 * <p>
 * Each {@code jti} is stored under the key {@code blacklist:<jti>} with a
 * TTL equal to the token's remaining lifetime. Redis handles eviction
 * automatically.
 */
@Component
@RequiredArgsConstructor
public class RedisTokenBlacklist implements TokenBlacklist {

    private static final String KEY_PREFIX = "blacklist:";
    private static final String VALUE = "1";

    private final ReactiveStringRedisTemplate redis;

    /**
     * {@inheritDoc}
     *
     * <p>If {@code ttl} is zero or negative the method returns without
     * touching Redis: a token that already expired does not need to be
     * blacklisted (Spring Security rejects it via {@code exp} anyway), and
     * persisting it with an invalid TTL could make Redis keep it forever.
     */
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
