package com.ej.subscript.infrastructure.security;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * {@link AuthenticatedOwnerResolver} implementation backed by the JWT that
 * Spring Security exposes as a {@link JwtAuthenticationToken} in the
 * request principal.
 *
 * <p>The token's {@code sub} claim carries the {@code ownerId} (see
 * {@link JwtService#generateAccessToken}). We parse it into a {@link UUID}
 * and return it so handlers can compare against path variables.
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
