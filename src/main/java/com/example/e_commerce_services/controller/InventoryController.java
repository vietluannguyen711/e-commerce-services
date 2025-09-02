package com.example.e_commerce_services.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_services.dto.VariantDto;
import com.example.e_commerce_services.service.InventoryService;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    // GET /inventory/availability?productSlug=ao-thun-basic
    // Body JSON (nếu dùng GET khó gửi body, bạn có thể đổi sang POST):
    // { "attrs": {"color":"black","size":"M"} }
    @PostMapping("/availability")
    public VariantDto availability(@RequestParam String productSlug,
            @RequestBody Map<String, String> attrs) {
        return service.checkAvailability(productSlug, attrs);
    }
}
