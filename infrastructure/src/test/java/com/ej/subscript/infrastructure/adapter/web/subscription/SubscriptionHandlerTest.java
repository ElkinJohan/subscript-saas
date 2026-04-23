package com.ej.subscript.infrastructure.adapter.web.subscription;

import com.ej.subscript.application.usecase.SubscriptionUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Subscription;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.test.web.reactive.server.HttpHandlerConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SubscriptionHandlerTest {

    private WebTestClient client;
    private SubscriptionUseCase subscriptionUseCase;

    private static final Money PRICE = new Money(new BigDecimal("80000"), "COP");
    private static final Subscription ACTIVE_SUB = Subscription.create(UUID.randomUUID(), UUID.randomUUID(), PRICE, 30);

    @BeforeEach
    void setUp() {
        subscriptionUseCase = Mockito.mock(SubscriptionUseCase.class);
        var handler = new SubscriptionHandler(subscriptionUseCase, Validation.buildDefaultValidatorFactory().getValidator());
        WebExceptionHandler exHandler = (exchange, ex) -> {
            if (ex instanceof BusinessException be)
                exchange.getResponse().setStatusCode(HttpStatus.valueOf(be.status()));
            else
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        };
        HttpHandler httpHandler = WebHttpHandlerBuilder
                .webHandler(RouterFunctions.toWebHandler(new SubscriptionRouter().subscriptionRoutes(handler)))
                .exceptionHandler(exHandler)
                .build();
        client = WebTestClient.bindToServer(new HttpHandlerConnector(httpHandler)).build();
    }

    @Test
    void shouldCreateSubscriptionAndReturn201() {
        when(subscriptionUseCase.create(any(), any())).thenReturn(Mono.just(ACTIVE_SUB));

        client.post().uri("/api/subscriptions")
                .bodyValue(new SubscriptionRequest(ACTIVE_SUB.clientId(), ACTIVE_SUB.planId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SubscriptionResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(ACTIVE_SUB.id());
                    assertThat(response.status()).isEqualTo("ACTIVE");
                });
    }

    @Test
    void shouldReturn400WhenClientIdIsNull() {
        client.post().uri("/api/subscriptions")
                .bodyValue(new SubscriptionRequest(null, UUID.randomUUID()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenPlanIdIsNull() {
        client.post().uri("/api/subscriptions")
                .bodyValue(new SubscriptionRequest(UUID.randomUUID(), null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldFindSubscriptionsByClientIdAndReturn200() {
        UUID clientId = ACTIVE_SUB.clientId();
        when(subscriptionUseCase.findByClientId(clientId)).thenReturn(Flux.just(ACTIVE_SUB));

        client.get().uri("/api/subscriptions/client/{clientId}", clientId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SubscriptionResponse.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).clientId()).isEqualTo(clientId);
                });
    }

    @Test
    void shouldFindActiveSubscriptionAndReturn200() {
        UUID clientId = ACTIVE_SUB.clientId();
        when(subscriptionUseCase.findActiveByClientId(clientId)).thenReturn(Mono.just(ACTIVE_SUB));

        client.get().uri("/api/subscriptions/client/{clientId}/active", clientId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionResponse.class)
                .value(response -> assertThat(response.status()).isEqualTo("ACTIVE"));
    }

    @Test
    void shouldCancelSubscriptionAndReturn200() {
        Subscription cancelled = ACTIVE_SUB.cancel();
        when(subscriptionUseCase.cancel(ACTIVE_SUB.id())).thenReturn(Mono.just(cancelled));

        client.post().uri("/api/subscriptions/{id}/cancel", ACTIVE_SUB.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionResponse.class)
                .value(response -> assertThat(response.status()).isEqualTo("CANCELLED"));
    }

    @Test
    void shouldRenewSubscriptionAndReturn200() {
        Subscription renewed = ACTIVE_SUB.renew(30);
        when(subscriptionUseCase.renew(ACTIVE_SUB.id())).thenReturn(Mono.just(renewed));

        client.post().uri("/api/subscriptions/{id}/renew", ACTIVE_SUB.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(SubscriptionResponse.class)
                .value(response -> {
                    assertThat(response.status()).isEqualTo("ACTIVE");
                    assertThat(response.startDate()).isEqualTo(ACTIVE_SUB.period().endDate());
                });
    }

    @Test
    void shouldReturn404WhenSubscriptionNotFoundOnCancel() {
        UUID id = UUID.randomUUID();
        when(subscriptionUseCase.cancel(id)).thenReturn(
                Mono.error(new BusinessException("Suscripción no encontrada", 404, "No existe"))
        );

        client.post().uri("/api/subscriptions/{id}/cancel", id)
                .exchange()
                .expectStatus().isNotFound();
    }
}
