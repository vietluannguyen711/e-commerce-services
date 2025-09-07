package com.example.e_commerce_services.controller;

// src/main/java/.../web/PingController.java
import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "ok " + Instant.now();
    }
}
