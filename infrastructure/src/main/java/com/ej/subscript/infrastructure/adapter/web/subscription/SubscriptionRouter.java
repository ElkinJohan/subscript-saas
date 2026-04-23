package com.ej.subscript.infrastructure.adapter.web.subscription;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SubscriptionRouter {

    @Bean
    public RouterFunction<ServerResponse> subscriptionRoutes(SubscriptionHandler handler) {
        return RouterFunctions.route()
                .POST("/api/subscriptions", handler::create)
                .GET("/api/subscriptions/client/{clientId}", handler::findByClientId)
                .GET("/api/subscriptions/client/{clientId}/active", handler::findActiveByClientId)
                .POST("/api/subscriptions/{id}/cancel", handler::cancel)
                .POST("/api/subscriptions/{id}/renew", handler::renew)
                .build();
    }
}
