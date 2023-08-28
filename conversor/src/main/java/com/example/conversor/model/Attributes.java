package com.example.conversor.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Map;

@Embeddable
@Data
public class Attributes {
    @ElementCollection
    @CollectionTable(name = "sucursal_attributes", joinColumns = @JoinColumn(name = "sucursal_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")

    private Map<String, OpeningHours> openingHours;
}
