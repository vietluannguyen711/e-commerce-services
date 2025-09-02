package com.example.e_commerce_services.dto;

public record ProductListItemDto(
        Long id,
        String slug,
        String title,
        String thumbnail, // ảnh đầu tiên (có thể null)
        Integer minPrice,
        Integer maxPrice
        ) {

}
