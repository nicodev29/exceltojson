package com.example.conversor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private UUID id;
    private UUID hub_id;

    private String code;
    private String type;
    private String number;
    private String name;
    private String manager;
    @JsonProperty("email_address")
    private String emailAddress;
    @JsonProperty("phone_number")
    private String phoneNumber;
    private String status;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Embedded
    private Address address;

    @Embedded
    private Attributes attributes;

}

