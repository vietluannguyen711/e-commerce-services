package com.example.e_commerce_services.dto;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceOrderRequest(
        @NotNull ShippingAddressDto shippingAddress,
        @NotBlank String paymentMethod, // "COD" (MVP)
        String note
) {}

