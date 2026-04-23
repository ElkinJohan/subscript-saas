package com.ej.subscript.infrastructure.adapter.web.plan;

import com.ej.subscript.application.usecase.PlanUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Money;
import com.ej.subscript.domain.model.Plan;
import com.ej.subscript.domain.model.PlanStatus;
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

class PlanHandlerTest {

    private WebTestClient client;
    private PlanUseCase planUseCase;

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final Plan ACTIVE_PLAN = new Plan(
            UUID.randomUUID(), OWNER_ID, "Mensual", "Acceso completo",
            new Money(new BigDecimal("80000"), "COP"), 30, PlanStatus.ACTIVE
    );

    @BeforeEach
    void setUp() {
        planUseCase = Mockito.mock(PlanUseCase.class);
        var handler = new PlanHandler(planUseCase, Validation.buildDefaultValidatorFactory().getValidator());
        WebExceptionHandler exHandler = (exchange, ex) -> {
            if (ex instanceof BusinessException be)
                exchange.getResponse().setStatusCode(HttpStatus.valueOf(be.status()));
            else
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        };
        HttpHandler httpHandler = WebHttpHandlerBuilder
                .webHandler(RouterFunctions.toWebHandler(new PlanRouter().planRoutes(handler)))
                .exceptionHandler(exHandler)
                .build();
        client = WebTestClient.bindToServer(new HttpHandlerConnector(httpHandler)).build();
    }

    @Test
    void shouldCreatePlanAndReturn201() {
        when(planUseCase.create(any())).thenReturn(Mono.just(ACTIVE_PLAN));

        client.post().uri("/api/owners/{ownerId}/plans", OWNER_ID)
                .bodyValue(new PlanRequest("Mensual", "Acceso completo", new BigDecimal("80000"), "COP", 30))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PlanResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(ACTIVE_PLAN.id());
                    assertThat(response.name()).isEqualTo("Mensual");
                    assertThat(response.status()).isEqualTo("ACTIVE");
                });
    }

    @Test
    void shouldReturn400WhenNameIsBlank() {
        client.post().uri("/api/owners/{ownerId}/plans", OWNER_ID)
                .bodyValue(new PlanRequest("", "Desc", new BigDecimal("80000"), "COP", 30))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenPriceIsNull() {
        client.post().uri("/api/owners/{ownerId}/plans", OWNER_ID)
                .bodyValue(new PlanRequest("Mensual", "Desc", null, "COP", 30))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenCurrencyIsBlank() {
        client.post().uri("/api/owners/{ownerId}/plans", OWNER_ID)
                .bodyValue(new PlanRequest("Mensual", "Desc", new BigDecimal("80000"), "", 30))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldFindPlansByOwnerIdAndReturn200() {
        when(planUseCase.findByOwnerId(OWNER_ID)).thenReturn(Flux.just(ACTIVE_PLAN));

        client.get().uri("/api/owners/{ownerId}/plans", OWNER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PlanResponse.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).ownerId()).isEqualTo(OWNER_ID);
                });
    }

    @Test
    void shouldDeactivatePlanAndReturn200() {
        Plan inactive = new Plan(ACTIVE_PLAN.id(), OWNER_ID, "Mensual", "Acceso completo",
                ACTIVE_PLAN.price(), 30, PlanStatus.INACTIVE);
        when(planUseCase.deactivate(ACTIVE_PLAN.id())).thenReturn(Mono.just(inactive));

        client.patch().uri("/api/plans/{id}/deactivate", ACTIVE_PLAN.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(PlanResponse.class)
                .value(response -> assertThat(response.status()).isEqualTo("INACTIVE"));
    }

    @Test
    void shouldReturn404WhenPlanNotFound() {
        UUID id = UUID.randomUUID();
        when(planUseCase.deactivate(id)).thenReturn(
                Mono.error(new BusinessException("Plan no encontrado", 404, "No existe"))
        );

        client.patch().uri("/api/plans/{id}/deactivate", id)
                .exchange()
                .expectStatus().isNotFound();
    }
}
