package com.ej.subscript.infrastructure.adapter.web.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Define las rutas funcionales del agregado Client y publica su contrato OpenAPI.
 *
 * <p>Sigue el patrón establecido por {@code AuthRouter} y {@code OwnerRouter}:
 * routing con {@link RouterFunctions} y metadata {@link RouterOperations}
 * coexistiendo en el mismo archivo. Las tres operaciones cuelgan de
 * {@code /api/owners/{ownerId}/clients/...} para que la relación padre-hijo
 * sea explícita en la URL — eso permite validar autorización a nivel de fila
 * (caller owner == path owner) sin necesidad de cargar el recurso primero.
 */
@Configuration
public class ClientRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/owners/{ownerId}/clients",
                    method = RequestMethod.POST,
                    beanClass = ClientHandler.class,
                    beanMethod = "register",
                    operation = @Operation(
                            operationId = "registerClient",
                            tags = {"Client"},
                            summary = "Register a new client under an owner",
                            description = "Creates a Client tied to the given owner. Requires a "
                                    + "valid Bearer access token. The client is the end customer "
                                    + "of the owner's business (e.g. a gym member).",
                            security = @SecurityRequirement(name = "bearerAuth"),
                            parameters = {
                                    @Parameter(
                                            name = "ownerId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Owner UUID",
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = ClientRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Client created",
                                            content = @Content(schema = @Schema(implementation = ClientResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content),
                                    @ApiResponse(responseCode = "401",
                                            description = "Missing or invalid access token",
                                            content = @Content),
                                    @ApiResponse(responseCode = "403",
                                            description = "The caller's owner id does not match the path's ownerId",
                                            content = @Content),
                                    @ApiResponse(responseCode = "404",
                                            description = "Owner not found",
                                            content = @Content),
                                    @ApiResponse(responseCode = "409",
                                            description = "Client cedula already registered for this owner",
                                            content = @Content)
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/owners/{ownerId}/clients",
                    method = RequestMethod.GET,
                    beanClass = ClientHandler.class,
                    beanMethod = "findByOwnerId",
                    operation = @Operation(
                            operationId = "listClientsByOwner",
                            tags = {"Client"},
                            summary = "List all clients for an owner",
                            description = "Returns the full set of clients (active and inactive) "
                                    + "belonging to the given owner.",
                            security = @SecurityRequirement(name = "bearerAuth"),
                            parameters = {
                                    @Parameter(
                                            name = "ownerId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Owner UUID",
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Client list",
                                            content = @Content(
                                                    array = @ArraySchema(
                                                            schema = @Schema(implementation = ClientResponse.class)))),
                                    @ApiResponse(responseCode = "401",
                                            description = "Missing or invalid access token",
                                            content = @Content)
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/owners/{ownerId}/clients/{clientId}/deactivate",
                    method = RequestMethod.PATCH,
                    beanClass = ClientHandler.class,
                    beanMethod = "deactivate",
                    operation = @Operation(
                            operationId = "deactivateClient",
                            tags = {"Client"},
                            summary = "Mark a client as inactive",
                            description = "Soft deactivation: the client record stays in the "
                                    + "database, but its status flips to INACTIVE so it stops "
                                    + "showing up as an active customer. Idempotent. The "
                                    + "{ownerId} segment makes the parent-child relation "
                                    + "explicit and enables row-level authorization on the "
                                    + "URL itself.",
                            security = @SecurityRequirement(name = "bearerAuth"),
                            parameters = {
                                    @Parameter(
                                            name = "ownerId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Owner UUID",
                                            schema = @Schema(type = "string", format = "uuid")
                                    ),
                                    @Parameter(
                                            name = "clientId",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Client UUID",
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Client deactivated",
                                            content = @Content(schema = @Schema(implementation = ClientResponse.class))),
                                    @ApiResponse(responseCode = "401",
                                            description = "Missing or invalid access token",
                                            content = @Content),
                                    @ApiResponse(responseCode = "403",
                                            description = "The caller's owner id does not match the path's ownerId",
                                            content = @Content),
                                    @ApiResponse(responseCode = "404", description = "Client not found",
                                            content = @Content)
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> clientRoutes(ClientHandler handler) {
        return RouterFunctions.route()
                .POST("/api/owners/{ownerId}/clients", handler::register)
                .GET("/api/owners/{ownerId}/clients", handler::findByOwnerId)
                .PATCH("/api/owners/{ownerId}/clients/{clientId}/deactivate", handler::deactivate)
                .build();
    }
}
