package com.example.e_commerce_services.dto;

public record CartItemDto(
        Long itemId,
        String sku,
        String productSlug,
        String title,
        String thumbnail,
        Integer unitPrice,
        Integer qty,
        Integer subtotal
        ) {

}
