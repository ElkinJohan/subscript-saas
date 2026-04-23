package com.ej.subscript.infrastructure.adapter.r2dbc.client;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("clients")
public record ClientEntity(
        @Id UUID id,
        @Column("owner_id") UUID ownerId,
        String cedula,
        String name,
        String email,
        String phone,
        String status
) {}
