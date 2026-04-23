package com.ej.subscript.infrastructure.adapter.web.plan;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PlanRouter {

    @Bean
    public RouterFunction<ServerResponse> planRoutes(PlanHandler handler) {
        return RouterFunctions.route()
                .POST("/api/owners/{ownerId}/plans", handler::create)
                .GET("/api/owners/{ownerId}/plans", handler::findByOwnerId)
                .PATCH("/api/plans/{id}/deactivate", handler::deactivate)
                .build();
    }
}
