package com.ej.subscript.infrastructure.adapter.web.payment;

import com.ej.subscript.application.usecase.PaymentUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Payment;
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

class PaymentHandlerTest {

    private WebTestClient client;
    private PaymentUseCase paymentUseCase;

    private static final Money PRICE = new Money(new BigDecimal("80000"), "COP");
    private static final UUID SUBSCRIPTION_ID = UUID.randomUUID();
    private static final UUID REGISTERED_BY = UUID.randomUUID();
    private static final Payment PAYMENT = Payment.register(SUBSCRIPTION_ID, PRICE, REGISTERED_BY);

    @BeforeEach
    void setUp() {
        paymentUseCase = Mockito.mock(PaymentUseCase.class);
        var handler = new PaymentHandler(paymentUseCase, Validation.buildDefaultValidatorFactory().getValidator());
        WebExceptionHandler exHandler = (exchange, ex) -> {
            if (ex instanceof BusinessException be)
                exchange.getResponse().setStatusCode(HttpStatus.valueOf(be.status()));
            else
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        };
        HttpHandler httpHandler = WebHttpHandlerBuilder
                .webHandler(RouterFunctions.toWebHandler(new PaymentRouter().paymentRoutes(handler)))
                .exceptionHandler(exHandler)
                .build();
        client = WebTestClient.bindToServer(new HttpHandlerConnector(httpHandler)).build();
    }

    @Test
    void shouldRegisterPaymentAndReturn201() {
        when(paymentUseCase.register(any(), any())).thenReturn(Mono.just(PAYMENT));

        client.post().uri("/api/payments")
                .bodyValue(new PaymentRequest(SUBSCRIPTION_ID, REGISTERED_BY))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PaymentResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(PAYMENT.id());
                    assertThat(response.subscriptionId()).isEqualTo(SUBSCRIPTION_ID);
                    assertThat(response.amount()).isEqualByComparingTo(new BigDecimal("80000"));
                    assertThat(response.currency()).isEqualTo("COP");
                });
    }

    @Test
    void shouldReturn400WhenSubscriptionIdIsNull() {
        client.post().uri("/api/payments")
                .bodyValue(new PaymentRequest(null, REGISTERED_BY))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenRegisteredByIsNull() {
        client.post().uri("/api/payments")
                .bodyValue(new PaymentRequest(SUBSCRIPTION_ID, null))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldFindPaymentsBySubscriptionIdAndReturn200() {
        when(paymentUseCase.findBySubscriptionId(SUBSCRIPTION_ID)).thenReturn(Flux.just(PAYMENT));

        client.get().uri("/api/payments/subscription/{subscriptionId}", SUBSCRIPTION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PaymentResponse.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).subscriptionId()).isEqualTo(SUBSCRIPTION_ID);
                });
    }

    @Test
    void shouldReturn404WhenSubscriptionNotFound() {
        when(paymentUseCase.register(any(), any())).thenReturn(
                Mono.error(new BusinessException("Suscripción no encontrada", 404, "No existe"))
        );

        client.post().uri("/api/payments")
                .bodyValue(new PaymentRequest(UUID.randomUUID(), REGISTERED_BY))
                .exchange()
                .expectStatus().isNotFound();
    }
}
