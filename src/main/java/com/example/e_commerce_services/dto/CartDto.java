package com.example.e_commerce_services.dto;

import java.util.List;

public record CartDto(
        Long id,
        List<CartItemDto> items,
        Summary summary
        ) {

    public record Summary(Integer subtotal, Integer shippingFee, Integer total) {

    }
}
