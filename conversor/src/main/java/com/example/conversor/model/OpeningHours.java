package com.example.conversor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
public class OpeningHours {

    @JsonProperty("opens")
    private String opens;
    @JsonProperty("closes")
    private String closes;

}
