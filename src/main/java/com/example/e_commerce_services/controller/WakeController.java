package com.example.e_commerce_services.controller;

// src/main/java/.../web/WakeController.java
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_services.repository.ProductRepository;

@RestController
public class WakeController {

    private final ProductRepository productRepo;

    public WakeController(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    // Gọi DB nhẹ nhàng để giữ pool/connection ấm
    @GetMapping("/_wake")
    @Transactional(readOnly = true)
    public String wake() {
        long n = productRepo.count(); // chạm DB
        return "ok:" + n;
    }
}
