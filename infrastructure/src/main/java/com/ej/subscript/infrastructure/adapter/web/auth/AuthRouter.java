package com.ej.subscript.infrastructure.adapter.web.auth;

import io.swagger.v3.oas.annotations.Operation;
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
public class AuthRouter {

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/auth/login",
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "login",
                    operation = @Operation(
                            operationId = "login",
                            tags = {"Auth"},
                            summary = "Authenticate owner and issue access + refresh tokens",
                            description = "Validates credentials with BCrypt and returns a 15-minute "
                                    + "access token plus a 7-day refresh token. Returns the same 401 "
                                    + "for unknown email and wrong password to prevent user enumeration.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Tokens issued",
                                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content),
                                    @ApiResponse(responseCode = "401", description = "Invalid credentials",
                                            content = @Content)
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/auth/refresh",
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "refresh",
                    operation = @Operation(
                            operationId = "refresh",
                            tags = {"Auth"},
                            summary = "Rotate the refresh token and issue a new pair",
                            description = "Validates the incoming refresh token, blacklists it, and "
                                    + "issues a new access + refresh pair. If the same token is "
                                    + "presented twice it is treated as reuse: the request is rejected "
                                    + "and an AUTH_TOKEN_REUSE_DETECTED audit event is recorded.",
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = RefreshRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "New token pair issued",
                                            content = @Content(schema = @Schema(implementation = TokenResponse.class))),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content),
                                    @ApiResponse(responseCode = "401",
                                            description = "Invalid, expired or revoked refresh token",
                                            content = @Content)
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/auth/logout",
                    method = RequestMethod.POST,
                    beanClass = AuthHandler.class,
                    beanMethod = "logout",
                    operation = @Operation(
                            operationId = "logout",
                            tags = {"Auth"},
                            summary = "Revoke the current access + refresh token pair",
                            description = "Adds both tokens to the Redis blacklist with a TTL "
                                    + "equal to their remaining lifetime. Requires a valid Bearer "
                                    + "access token in the Authorization header.",
                            security = @SecurityRequirement(name = "bearerAuth"),
                            requestBody = @RequestBody(
                                    required = true,
                                    content = @Content(schema = @Schema(implementation = RefreshRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "204", description = "Logout successful"),
                                    @ApiResponse(responseCode = "400", description = "Validation error",
                                            content = @Content),
                                    @ApiResponse(responseCode = "401",
                                            description = "Missing or invalid access token",
                                            content = @Content)
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> authRoutes(AuthHandler handler) {
        return RouterFunctions.route()
                .POST("/api/auth/login", handler::login)
                .POST("/api/auth/refresh", handler::refresh)
                .POST("/api/auth/logout", handler::logout)
                .build();
    }
}
