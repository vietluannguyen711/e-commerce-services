package com.example.e_commerce_services.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record MergeCartRequest(List<MergeItem> items) {

    public record MergeItem(@NotBlank
            String sku, @Min(1)
            int qty) {

    }
}
