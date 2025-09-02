package com.example.e_commerce_services.dto;



public record OrderListItemDto(
        Long id,
        String number,
        String status,
        Integer total,
        String createdAt
) {}
