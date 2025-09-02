package com.example.e_commerce_services.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record AddItemRequest(
        @NotBlank
        String sku,
        @Min(1)
        int qty
        ) {

}
