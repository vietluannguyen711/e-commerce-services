package com.example.e_commerce_services.dto;

import java.util.List;
import java.util.Map;

public record ProductDetailDto(
        Long id,
        String slug,
        String title,
        String description,
        String brand,
        List<String> images,
        Map<String, List<String>> attributes, // ví dụ: color -> [black, white], size -> [M, L, XL]
        List<VariantDto> variants
        ) {

}
