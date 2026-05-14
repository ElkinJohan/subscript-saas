package com.ej.subscript.infrastructure.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementación de {@link AuthenticatedOwnerResolver} respaldada por el JWT
 * que Spring Security expone como {@link JwtAuthenticationToken} en el
 * principal del request.
 *
 * <p>El claim {@code sub} del token contiene el {@code ownerId} (ver
 * {@link JwtService#generateAccessToken}). Acá lo parseamos a {@link UUID}
 * y lo devolvemos para que los handlers puedan compararlo contra los path
 * variables.
 */
@Component
public class JwtAuthenticatedOwnerResolver implements AuthenticatedOwnerResolver {

    @Override
    public Mono<UUID> currentOwnerId(ServerRequest request) {
        return request.principal()
                .cast(JwtAuthenticationToken.class)
                .map(token -> UUID.fromString(token.getToken().getSubject()));
    }
}
