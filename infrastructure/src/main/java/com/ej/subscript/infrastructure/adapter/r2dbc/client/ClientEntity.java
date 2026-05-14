package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * Representación R2DBC del Client para la tabla {@code clients}.
 *
 * <p>Implementa {@link Persistable} para que Spring Data sepa cuándo emitir
 * INSERT vs UPDATE. El UUID del agregado se genera en el dominio antes de
 * persistir, así que sin el flag {@code isNew} R2DBC asumiría que la fila ya
 * existe y haría UPDATE en un INSERT. {@code ClientMapper} setea el flag
 * según la operación: {@code true} en {@link ClientMapper#toEntity} (save),
 * {@code false} en {@link ClientMapper#toEntityForUpdate} (update).
 *
 * <p>El {@code status} se mapea como {@link String}, no como el enum
 * {@code ClientStatus}, para evitar acoplar el schema de la base al tipo
 * Java; la traducción enum↔string vive en el mapper.
 */
@Table("clients")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientEntity implements Persistable<UUID> {

    @Id
    private UUID id;
    @Column("owner_id")
    private UUID ownerId;
    private String cedula;
    private String name;
    private String email;
    private String phone;
    private String status;
    @Transient
    private boolean isNew;
}
