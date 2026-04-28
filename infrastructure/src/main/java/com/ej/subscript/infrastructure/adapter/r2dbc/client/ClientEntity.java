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
