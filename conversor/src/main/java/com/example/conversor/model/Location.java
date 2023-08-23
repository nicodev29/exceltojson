package com.example.conversor.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.List;

@Embeddable
@Data
public class Location {
    @Column(name = "location_type")
    private String type;


    @ElementCollection
    private List<Double> coordinates;
}
