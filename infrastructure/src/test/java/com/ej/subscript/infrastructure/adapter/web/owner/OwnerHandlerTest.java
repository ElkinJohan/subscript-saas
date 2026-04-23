package com.ej.subscript.infrastructure.adapter.web.owner;

import com.ej.subscript.application.usecase.OwnerUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.reactive.server.HttpHandlerConnector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OwnerHandlerTest {

    private WebTestClient client;
    private OwnerUseCase ownerUseCase;

    private static final Owner OWNER = new Owner(
            UUID.randomUUID(), "900123", "Juan", "juan@gym.com", "300", "GymFit", 3,
            "$2a$10$hashedPasswordForTests"
    );

    @BeforeEach
    void setUp() {
        ownerUseCase = Mockito.mock(OwnerUseCase.class);
        var handler = new OwnerHandler(
                ownerUseCase,
                Validation.buildDefaultValidatorFactory().getValidator(),
                new BCryptPasswordEncoder()
        );
        WebExceptionHandler exHandler = (exchange, ex) -> {
            if (ex instanceof BusinessException be)
                exchange.getResponse().setStatusCode(HttpStatus.valueOf(be.status()));
            else
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        };
        HttpHandler httpHandler = WebHttpHandlerBuilder
                .webHandler(RouterFunctions.toWebHandler(new OwnerRouter().ownerRoutes(handler)))
                .exceptionHandler(exHandler)
                .build();
        client = WebTestClient.bindToServer(new HttpHandlerConnector(httpHandler)).build();
    }

    @Test
    void shouldRegisterOwnerAndReturn201() {
        when(ownerUseCase.register(any())).thenReturn(Mono.just(OWNER));

        client.post().uri("/api/owners")
                .bodyValue(new OwnerRequest("900123", "Juan", "juan@gym.com", "300", "GymFit", 3, "password123"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(OwnerResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(OWNER.id());
                    assertThat(response.email()).isEqualTo(OWNER.email());
                    assertThat(response.businessName()).isEqualTo(OWNER.businessName());
                });
    }

    @Test
    void shouldReturn400WhenNitIsBlank() {
        client.post().uri("/api/owners")
                .bodyValue(new OwnerRequest("", "Juan", "juan@gym.com", "300", "GymFit", 3, "password123"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() {
        client.post().uri("/api/owners")
                .bodyValue(new OwnerRequest("900123", "Juan", "not-an-email", "300", "GymFit", 3, "password123"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenNameIsBlank() {
        client.post().uri("/api/owners")
                .bodyValue(new OwnerRequest("900123", "", "juan@gym.com", "300", "GymFit", 3, "password123"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenPasswordIsTooShort() {
        client.post().uri("/api/owners")
                .bodyValue(new OwnerRequest("900123", "Juan", "juan@gym.com", "300", "GymFit", 3, "short"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldFindOwnerByIdAndReturn200() {
        String id = OWNER.id().toString();
        when(ownerUseCase.findById(id)).thenReturn(Mono.just(OWNER));

        client.get().uri("/api/owners/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OwnerResponse.class)
                .value(response -> assertThat(response.id()).isEqualTo(OWNER.id()));
    }

    @Test
    void shouldReturn404WhenOwnerNotFound() {
        String id = UUID.randomUUID().toString();
        when(ownerUseCase.findById(id)).thenReturn(
                Mono.error(new BusinessException("Owner no encontrado", 404, "No existe"))
        );

        client.get().uri("/api/owners/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }
}
