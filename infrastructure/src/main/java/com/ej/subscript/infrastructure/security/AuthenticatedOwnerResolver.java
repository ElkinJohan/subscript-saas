package com.ej.subscript.infrastructure.security;

import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Extrae el {@code ownerId} del caller a partir del {@link ServerRequest}.
 *
 * <p>Existe como abstracción para desacoplar a los handlers del mecanismo de
 * autenticación concreto: hoy es un JWT con {@code sub = ownerId}, mañana
 * podría ser una sesión cookie-based o un header propietario sin que los
 * handlers tengan que cambiar.
 *
 * <p>Pensado para checks de autorización a nivel de fila — el caller comparte
 * el id obtenido acá contra el id presente en el path para decidir si tiene
 * acceso al recurso.
 */
public interface AuthenticatedOwnerResolver {

    /**
     * @param request request entrante con el principal ya validado por el filtro de seguridad.
     * @return {@link Mono} con el ownerId del caller; emite vacío si no hay principal
     *         (lo que en práctica no debería ocurrir en endpoints autenticados).
     */
    Mono<UUID> currentOwnerId(ServerRequest request);
}
