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
    private UUID id;
    private UUID parentId;

    private String code;
    private String type;
    private String number;
    private String name;
    private String manager;
    private String emailAddress;
    private String phoneNumber;
    private String status;
    private String numeration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Embedded
    private Address address;

    @ElementCollection
    @CollectionTable(name = "sucursal_attributes", joinColumns = @JoinColumn(name = "sucursal_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "hours", joinColumns = @JoinColumn(name = "sucursal_id"))
    @MapKeyColumn(name = "day_of_week")
    @AttributeOverrides({
            @AttributeOverride(name="opens", column=@Column(name="opens_at")),
            @AttributeOverride(name="closes", column=@Column(name="closes_at"))
    })
    private Map<String, DayHours> openingHours = new HashMap<>();



}

