package com.ecommerce.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Set;

@Entity
@NoArgsConstructor
@Data
@AllArgsConstructor
@Table(name = "addresses")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 characters long")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must be at least 5 characters long")
    private String buildingName;

    @NotBlank
    @Size(min = 5, message = "City name must be at least 5 characters long")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State name must be at least 2 characters long")
    private String state;

    @NotBlank
    @Size(min = 2, message = "Country name must be at least 2 characters long")
    private String country;

    @NotBlank
    @Size(min = 6, message = "City name must be at least 6 characters long")
    private String zipcode;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(String street, String buildingName, String city, String state, String country, String zipcode) {
        this.street = street;
        this.buildingName = buildingName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zipcode = zipcode;
    }
}
