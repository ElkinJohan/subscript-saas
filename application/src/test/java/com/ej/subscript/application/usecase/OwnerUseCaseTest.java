package com.ej.subscript.application.usecase;

import com.ej.subscript.domain.exception.BusinessException;
import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.domain.repository.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OwnerUseCaseTest {

    private OwnerRepository ownerRepository;
    private OwnerUseCase ownerUseCase;

    private static final Owner OWNER = new Owner(
            UUID.randomUUID(), "900123456", "Juan", "juan@gym.com",
            "300000000", "GymFit", 3, "$2a$10$hashedPasswordForTests"
    );

    @BeforeEach
    void setUp() {
        ownerRepository = Mockito.mock(OwnerRepository.class);
        ownerUseCase = new OwnerUseCase(ownerRepository);
    }

    @Test
    void shouldRegisterOwnerWhenEmailAndNitAreNew() {
        when(ownerRepository.findByEmail(OWNER.email())).thenReturn(Mono.empty());
        when(ownerRepository.findByNit(OWNER.nit())).thenReturn(Mono.empty());
        when(ownerRepository.save(any())).thenReturn(Mono.just(OWNER));

        StepVerifier.create(ownerUseCase.register(OWNER))
                .assertNext(result -> assertThat(result.email()).isEqualTo(OWNER.email()))
                .verifyComplete();
    }

    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        when(ownerRepository.findByEmail(OWNER.email())).thenReturn(Mono.just(OWNER));
        when(ownerRepository.findByNit(OWNER.nit())).thenReturn(Mono.empty()); // eager chain construction
        when(ownerRepository.save(any())).thenReturn(Mono.just(OWNER));        // eager chain construction

        StepVerifier.create(ownerUseCase.register(OWNER))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.status()).isEqualTo(409);
                    assertThat(be.title()).contains("Email");
                })
                .verify();
    }

    @Test
    void shouldRejectRegistrationWhenNitAlreadyExists() {
        when(ownerRepository.findByEmail(OWNER.email())).thenReturn(Mono.empty());
        when(ownerRepository.findByNit(OWNER.nit())).thenReturn(Mono.just(OWNER));
        when(ownerRepository.save(any())).thenReturn(Mono.just(OWNER));        // eager chain construction

        StepVerifier.create(ownerUseCase.register(OWNER))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    BusinessException be = (BusinessException) ex;
                    assertThat(be.status()).isEqualTo(409);
                    assertThat(be.title()).contains("NIT");
                })
                .verify();
    }

    @Test
    void shouldFindOwnerById() {
        String id = OWNER.id().toString();
        when(ownerRepository.findById(id)).thenReturn(Mono.just(OWNER));

        StepVerifier.create(ownerUseCase.findById(id))
                .assertNext(result -> assertThat(result.id()).isEqualTo(OWNER.id()))
                .verifyComplete();
    }

    @Test
    void shouldReturn404WhenOwnerNotFound() {
        String id = UUID.randomUUID().toString();
        when(ownerRepository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(ownerUseCase.findById(id))
                .expectErrorSatisfies(ex -> {
                    assertThat(ex).isInstanceOf(BusinessException.class);
                    assertThat(((BusinessException) ex).status()).isEqualTo(404);
                })
                .verify();
    }
}
