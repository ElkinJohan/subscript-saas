package com.ej.subscript.infrastructure.security;

import lombok.Getter;
import org.springframework.security.oauth2.jwt.BadJwtException;

/**
 * Señaliza que un JWT criptográficamente válido fue rechazado por estar en la blacklist.
 * <p>
 * Se distingue de {@link BadJwtException} para permitir que los handlers de aplicación
 * traten el reuso de un token revocado (señal de seguridad) de forma diferente que un
 * token con firma inválida o expirado (error común). El handler de {@code /refresh}
 * usa esta distinción para emitir un evento de auditoría {@code AUTH_TOKEN_REUSE_DETECTED}.
 */
@Getter
public class RevokedTokenException extends BadJwtException {

    private final String jti;
    private final String subject;

    public RevokedTokenException(String jti, String subject) {
        super("Token revocado");
        this.jti = jti;
        this.subject = subject;
    }
}
