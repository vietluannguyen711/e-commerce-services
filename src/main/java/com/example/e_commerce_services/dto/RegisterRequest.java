package com.example.e_commerce_services.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email
        @NotBlank
        String email,
        @NotBlank
        @Size(min = 6, max = 100)
        String password,
        @NotBlank
        String name
        ) {

}
