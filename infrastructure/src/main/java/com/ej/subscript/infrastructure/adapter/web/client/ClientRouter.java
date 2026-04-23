package com.ej.subscript.infrastructure.adapter.web.client;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ClientRouter {

    @Bean
    public RouterFunction<ServerResponse> clientRoutes(ClientHandler handler) {
        return RouterFunctions.route()
                .POST("/api/owners/{ownerId}/clients", handler::register)
                .GET("/api/owners/{ownerId}/clients", handler::findByOwnerId)
                .PATCH("/api/clients/{id}/deactivate", handler::deactivate)
                .build();
    }
}
