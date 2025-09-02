package com.example.e_commerce_services.dto;



import jakarta.validation.constraints.NotBlank;

public record ShippingAddressDto(
        @NotBlank String fullName,
        @NotBlank String phone,
        @NotBlank String line1,
        @NotBlank String province,
        @NotBlank String district,
        @NotBlank String ward
) {}

