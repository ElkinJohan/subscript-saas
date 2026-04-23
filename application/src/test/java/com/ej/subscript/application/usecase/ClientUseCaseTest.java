package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Client;
import com.ej.subscript.domain.model.ClientStatus;
import com.ej.subscript.domain.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ClientUseCaseTest {

    private ClientRepository clientRepository;
    private ClientUseCase clientUseCase;

    private static final UUID OWNER_ID = UUID.randomUUID();
    private static final Client ACTIVE_CLIENT = new Client(
            UUID.randomUUID(), OWNER_ID, "123456", "Carlos", "carlos@test.com", "300000000", ClientStatus.ACTIVE
    );

    @BeforeEach
    void setUp() {
        clientRepository = Mockito.mock(ClientRepository.class);
        clientUseCase = new ClientUseCase(clientRepository);
    }

    @Test
    void shouldRegisterClient() {
        when(clientRepository.save(any())).thenReturn(Mono.just(ACTIVE_CLIENT));

        StepVerifier.create(clientUseCase.register(ACTIVE_CLIENT))
                .assertNext(result -> {
                    assertThat(result.id()).isEqualTo(ACTIVE_CLIENT.id());
                    assertThat(result.status()).isEqualTo(ClientStatus.ACTIVE);
                })
                .verifyComplete();
    }

    @Test
    void shouldDeactivateClient() {
        Client inactive = new Client(
                ACTIVE_CLIENT.id(), OWNER_ID, "123456", "Carlos",
                "carlos@test.com", "300000000", ClientStatus.INACTIVE
        );
        when(clientRepository.findById(ACTIVE_CLIENT.id())).thenReturn(Mono.just(ACTIVE_CLIENT));
        when(clientRepository.update(any())).thenReturn(Mono.just(inactive));

        StepVerifier.create(clientUseCase.deactivate(ACTIVE_CLIENT.id()))
                .assertNext(result -> assertThat(result.status()).isEqualTo(ClientStatus.INACTIVE))
                .verifyComplete();
    }

    @Test
    void shouldReturn404WhenDeactivatingNonExistentClient() {
        UUID id = UUID.randomUUID();
        when(clientRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(clientUseCase.deactivate(id))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(404);
                })
                .verify();
    }

    @Test
    void shouldFindClientsByOwnerId() {
        when(clientRepository.findByOwnerId(OWNER_ID)).thenReturn(Flux.just(ACTIVE_CLIENT));

        StepVerifier.create(clientUseCase.findByOwnerId(OWNER_ID))
                .assertNext(result -> assertThat(result.ownerId()).isEqualTo(OWNER_ID))
                .verifyComplete();
    }
}
