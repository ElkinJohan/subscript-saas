package com.ej.subscript.infrastructure.security;

import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Extracts the caller's {@code ownerId} from a {@link ServerRequest}.
 *
 * <p>Exists as an abstraction so handlers stay decoupled from the concrete
 * authentication mechanism: today it is a JWT with {@code sub = ownerId},
 * tomorrow it could be a cookie-based session or a proprietary header
 * without forcing handlers to change.
 *
 * <p>Designed for row-level authorization checks — the caller compares the
 * id returned here against the id carried by the path to decide whether
 * access to the resource is allowed.
 */
public interface AuthenticatedOwnerResolver {

    /**
     * @param request incoming request whose principal has already been
     *                validated by the security filter chain.
     * @return {@link Mono} with the caller's ownerId; emits empty when no
     *         principal is present (which in practice should not happen on
     *         authenticated endpoints).
     */
    Mono<UUID> currentOwnerId(ServerRequest request);
}
