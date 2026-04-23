package com.ej.subscript.infrastructure.adapter.web.owner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class OwnerRouter {

    @Bean
    public RouterFunction<ServerResponse> ownerRoutes(OwnerHandler handler) {
        return RouterFunctions.route()
                .POST("/api/owners", handler::register)
                .GET("/api/owners/{id}", handler::findById)
                .build();
    }
}
