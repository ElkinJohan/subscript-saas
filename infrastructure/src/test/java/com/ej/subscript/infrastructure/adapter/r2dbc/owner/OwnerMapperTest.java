package com.ej.subscript.infrastructure.adapter.r2dbc.owner;

import com.ej.subscript.domain.model.Owner;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OwnerMapperTest {

    private static final Owner OWNER = new Owner(
            UUID.randomUUID(), "900123", "Juan", "juan@test.com", "300", "GymFit", 3
    );

    @Test
    void shouldMapDomainToEntity() {
        OwnerEntity entity = OwnerMapper.toEntity(OWNER);

        assertThat(entity.getId()).isEqualTo(OWNER.id());
        assertThat(entity.getNit()).isEqualTo(OWNER.nit());
        assertThat(entity.getName()).isEqualTo(OWNER.name());
        assertThat(entity.getEmail()).isEqualTo(OWNER.email());
        assertThat(entity.getPhone()).isEqualTo(OWNER.phone());
        assertThat(entity.getBusinessName()).isEqualTo(OWNER.businessName());
        assertThat(entity.getGracePeriodDays()).isEqualTo(OWNER.gracePeriodDays());
        assertThat(entity.isNew()).isTrue();
    }

    @Test
    void shouldMapEntityToDomain() {
        OwnerEntity entity = OwnerMapper.toEntity(OWNER);
        Owner result = OwnerMapper.toDomain(entity);

        assertThat(result.id()).isEqualTo(OWNER.id());
        assertThat(result.nit()).isEqualTo(OWNER.nit());
        assertThat(result.email()).isEqualTo(OWNER.email());
        assertThat(result.businessName()).isEqualTo(OWNER.businessName());
    }

    @Test
    void shouldRoundTripWithoutDataLoss() {
        Owner result = OwnerMapper.toDomain(OwnerMapper.toEntity(OWNER));
        assertThat(result).isEqualTo(OWNER);
    }
}
