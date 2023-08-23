package com.example.conversor.model;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class DayHours extends OpeningHours {
    private String opens;
    private String closes;

}
