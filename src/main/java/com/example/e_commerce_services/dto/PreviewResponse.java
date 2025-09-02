package com.example.e_commerce_services.dto;

import java.util.List;

public record PreviewResponse(
        List<PreviewItem> items,
        Integer shippingFee,
        Integer subtotal,
        Integer total,
        boolean canPlaceOrder
        ) {

    public record PreviewItem(String sku, Integer qty, Integer unitPrice, Integer lineTotal) {

    }
}
