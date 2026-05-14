package com.ej.subscript.infrastructure.adapter.web.client;

import com.ej.subscript.application.usecase.ClientUseCase;
import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.model.ClientStatus;
import com.ej.subscript.infrastructure.security.AuthenticatedOwnerResolver;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClientHandlerTest {

    private WebTestClient client;
    private ClientUseCase clientUseCase;
    private AuthenticatedOwnerResolver authenticatedOwnerResolver;

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final Client ACTIVE_CLIENT = new Client(
            UUID.randomUUID(), OWNER_ID, "123456", "Carlos", "carlos@test.com", "300", ClientStatus.ACTIVE
    );

    @BeforeEach
    void setUp() {
        clientUseCase = Mockito.mock(ClientUseCase.class);
        authenticatedOwnerResolver = Mockito.mock(AuthenticatedOwnerResolver.class);
        // Default: caller is OWNER_ID. Cross-owner tests override per case.
        when(authenticatedOwnerResolver.currentOwnerId(any())).thenReturn(Mono.just(OWNER_ID));
        var handler = new ClientHandler(
                clientUseCase,
                Validation.buildDefaultValidatorFactory().getValidator(),
                authenticatedOwnerResolver
        );
        WebExceptionHandler exHandler = (exchange, ex) -> {
            if (ex instanceof BusinessException be)
                exchange.getResponse().setStatusCode(HttpStatus.valueOf(be.status()));
            else
                exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return exchange.getResponse().setComplete();
        };
        HttpHandler httpHandler = WebHttpHandlerBuilder
                .webHandler(RouterFunctions.toWebHandler(new ClientRouter().clientRoutes(handler)))
                .exceptionHandler(exHandler)
                .build();
        client = WebTestClient.bindToServer(new HttpHandlerConnector(httpHandler)).build();
    }

    @Test
    void shouldRegisterClientAndReturn201() {
        when(clientUseCase.register(any())).thenReturn(Mono.just(ACTIVE_CLIENT));

        client.post().uri("/api/owners/{ownerId}/clients", OWNER_ID)
                .bodyValue(new ClientRequest("123456", "Carlos", "carlos@test.com", "300"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ClientResponse.class)
                .value(response -> {
                    assertThat(response.id()).isEqualTo(ACTIVE_CLIENT.id());
                    assertThat(response.ownerId()).isEqualTo(OWNER_ID);
                    assertThat(response.status()).isEqualTo("ACTIVE");
                });
    }

    @Test
    void shouldReturn400WhenCedulaIsBlank() {
        client.post().uri("/api/owners/{ownerId}/clients", OWNER_ID)
                .bodyValue(new ClientRequest("", "Carlos", "carlos@test.com", "300"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() {
        client.post().uri("/api/owners/{ownerId}/clients", OWNER_ID)
                .bodyValue(new ClientRequest("123456", "Carlos", "not-an-email", "300"))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldFindClientsByOwnerIdAndReturn200() {
        when(clientUseCase.findByOwnerId(OWNER_ID)).thenReturn(Flux.just(ACTIVE_CLIENT));

        client.get().uri("/api/owners/{ownerId}/clients", OWNER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ClientResponse.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).ownerId()).isEqualTo(OWNER_ID);
                });
    }

    @Test
    void shouldDeactivateClientAndReturn200() {
        Client inactive = new Client(ACTIVE_CLIENT.id(), OWNER_ID, "123456", "Carlos",
                "carlos@test.com", "300", ClientStatus.INACTIVE);
        when(clientUseCase.deactivate(ACTIVE_CLIENT.id())).thenReturn(Mono.just(inactive));

        client.patch().uri("/api/owners/{ownerId}/clients/{clientId}/deactivate",
                        OWNER_ID, ACTIVE_CLIENT.id())
                .exchange()
                .expectStatus().isOk()
                .expectBody(ClientResponse.class)
                .value(response -> assertThat(response.status()).isEqualTo("INACTIVE"));
    }

    @Test
    void shouldReturn404WhenDeactivatingNonExistentClient() {
        UUID id = UUID.randomUUID();
        when(clientUseCase.deactivate(id)).thenReturn(
                Mono.error(new BusinessException("Cliente no encontrado", 404, "No existe"))
        );

        client.patch().uri("/api/owners/{ownerId}/clients/{clientId}/deactivate", OWNER_ID, id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn403WhenRegisteringClientForDifferentOwner() {
        UUID otherOwnerId = UUID.randomUUID();
        // caller is OWNER_ID (default mock) but path targets otherOwnerId

        client.post().uri("/api/owners/{ownerId}/clients", otherOwnerId)
                .bodyValue(new ClientRequest("123456", "Carlos", "carlos@test.com", "300"))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn403WhenListingClientsOfDifferentOwner() {
        UUID otherOwnerId = UUID.randomUUID();

        client.get().uri("/api/owners/{ownerId}/clients", otherOwnerId)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void shouldReturn403WhenDeactivatingClientOfDifferentOwner() {
        UUID otherOwnerId = UUID.randomUUID();

        client.patch().uri("/api/owners/{ownerId}/clients/{clientId}/deactivate",
                        otherOwnerId, ACTIVE_CLIENT.id())
                .exchange()
                .expectStatus().isForbidden();
    }
}
