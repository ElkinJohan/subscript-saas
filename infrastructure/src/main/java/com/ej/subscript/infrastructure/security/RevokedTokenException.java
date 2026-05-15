package com.ej.subscript.infrastructure.security;

import lombok.Getter;
import org.springframework.security.oauth2.jwt.BadJwtException;

/**
 * Signals that a cryptographically valid JWT was rejected because it sits
 * in the blacklist.
 * <p>
 * Distinguished from {@link BadJwtException} so application handlers can
 * treat a revoked-token reuse (a security signal) differently from a token
 * with an invalid signature or an expired one (ordinary errors). The
 * {@code /refresh} handler relies on this distinction to emit an
 * {@code AUTH_TOKEN_REUSE_DETECTED} audit event.
 */
@Getter
public class RevokedTokenException extends BadJwtException {

    private final String jti;
    private final String subject;

    public RevokedTokenException(String jti, String subject) {
        super("Revoked token");
        this.jti = jti;
        this.subject = subject;
    }
}
