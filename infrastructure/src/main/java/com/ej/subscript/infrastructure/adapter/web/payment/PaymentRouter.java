package com.ej.subscript.infrastructure.adapter.web.payment;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PaymentRouter {

    @Bean
    public RouterFunction<ServerResponse> paymentRoutes(PaymentHandler handler) {
        return RouterFunctions.route()
                .POST("/api/payments", handler::register)
                .GET("/api/payments/subscription/{subscriptionId}", handler::findBySubscriptionId)
                .build();
    }
}
