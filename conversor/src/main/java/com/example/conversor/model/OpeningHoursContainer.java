package com.example.conversor.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Embeddable
@Data
public class OpeningHoursContainer {
    private Map<String, Map<String, String>> openingHours = new HashMap<>();

}

