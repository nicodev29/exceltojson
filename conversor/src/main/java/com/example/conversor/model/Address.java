package com.example.conversor.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.Data;

@Embeddable
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {
    @JsonProperty("address_line_1")
    private String addressLine1;
    @JsonProperty("address_line_2")
    private String addressLine2;
    private String city;
    private String region;
    @JsonProperty("postal_code")
    private String postalCode;
    private String country;
    private String numeration;

    @Embedded
    private Location location;
}
