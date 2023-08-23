package com.example.conversor.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table
(name = "sucursales")
public class Sucursal {

    @Id
    private UUID id;  // Importa java.util.UUID
    private UUID parentId;

    private String code;
    private String type;
    private String number;
    private String name;
    private String manager;
    private String emailAddress;
    private String phoneNumber;
    private String status;
    private LocalDateTime createdAt;  // Importa java.time.LocalDateTime
    private LocalDateTime updatedAt;

    @Embedded
    private Address address;

    @ElementCollection
    @CollectionTable(name = "sucursal_attributes", joinColumns = @JoinColumn(name = "sucursal_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    // Si necesitas representar los atributos, servicios y exclusiones como objetos o listas, deberás crear clases o colecciones adicionales.
}

