package com.example.e_commerce_services.dto;

import java.util.Map;

public record VariantDto(
        String sku,
        Integer price,
        Integer stock,
        Map<String, String> attrs
        ) {

}
