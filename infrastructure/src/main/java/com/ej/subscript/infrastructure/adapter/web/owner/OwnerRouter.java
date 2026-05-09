package com.ej.subscript.infrastructure.adapter.web.owner;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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

@Configuration
public class OwnerRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/owners",
                    method = RequestMethod.POST,
                    beanClass = OwnerHandler.class,
                    beanMethod = "register",
                    operation = @Operation(
                            operationId = "registerOwner",
                            tags = {"Owner"},
                            summary = "Register a new owner (business account)",
                            description = "Public endpoint. Creates an Owner with a BCrypt-hashed "
                                    + "password. The Owner represents a business (gym, academy, "
                                    + "clinic) that will manage its own clients and subscriptions.",
                            security = {},
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = OwnerRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Owner created",
                                            content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content),
                                    @ApiResponse(responseCode = "409",
                                            description = "Email or NIT already registered",
                                            content = @Content)
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/owners/{id}",
                    method = RequestMethod.GET,
                    beanClass = OwnerHandler.class,
                    beanMethod = "findById",
                    operation = @Operation(
                            operationId = "findOwnerById",
                            tags = {"Owner"},
                            summary = "Fetch an owner by id",
                            description = "Returns the owner profile. Requires a valid Bearer "
                                    + "access token in the Authorization header.",
                            security = @SecurityRequirement(name = "bearerAuth"),
                            parameters = {
                                    @Parameter(
                                            name = "id",
                                            in = ParameterIn.PATH,
                                            required = true,
                                            description = "Owner UUID",
                                            schema = @Schema(type = "string", format = "uuid")
                                    )
                            },
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Owner found",
                                            content = @Content(schema = @Schema(implementation = OwnerResponse.class))),
                                    @ApiResponse(responseCode = "401",
                                            description = "Missing or invalid access token",
                                            content = @Content),
                                    @ApiResponse(responseCode = "404", description = "Owner not found",
                                            content = @Content)
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> ownerRoutes(OwnerHandler handler) {
        return RouterFunctions.route()
                .POST("/api/owners", handler::register)
                .GET("/api/owners/{id}", handler::findById)
                .build();
    }
}
