package com.example.e_commerce_services.dto;

import jakarta.validation.constraints.Min;

public record UpdateQtyRequest(@Min(1)
        int qty) {

}
