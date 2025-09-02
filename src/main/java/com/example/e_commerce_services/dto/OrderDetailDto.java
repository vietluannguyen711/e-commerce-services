package com.example.e_commerce_services.dto;



import java.util.List;

public record OrderDetailDto(
        Long id,
        String number,
        String status,
        List<Item> items,
        Shipping shipping,
        Integer shippingFee,
        Integer subtotal,
        Integer total,
        String createdAt
) {
    public record Item(String sku, String title, Integer qty, Integer unitPrice, Integer lineTotal) {}
    public record Shipping(String fullName, String phone, String line1, String province, String district, String ward) {}
}

