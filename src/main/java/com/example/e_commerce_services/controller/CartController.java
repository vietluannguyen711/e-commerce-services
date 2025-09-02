package com.example.e_commerce_services.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_services.dto.AddItemRequest;
import com.example.e_commerce_services.dto.CartDto;
import com.example.e_commerce_services.dto.MergeCartRequest;
import com.example.e_commerce_services.dto.UpdateQtyRequest;
import com.example.e_commerce_services.security.UserPrincipal;
import com.example.e_commerce_services.service.CartService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping
    public CartDto get(@AuthenticationPrincipal UserPrincipal user) {
        return service.getCart(user.getId());
    }

    @PostMapping("/items")
    public CartDto addItem(@AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody AddItemRequest req) {
        return service.addItem(user.getId(), req.sku(), req.qty());
    }

    @PatchMapping("/items/{itemId}")
    public CartDto updateQty(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateQtyRequest req) {
        return service.updateQty(user.getId(), itemId, req.qty());
    }

    @DeleteMapping("/items/{itemId}")
    public void deleteItem(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long itemId) {
        service.removeItem(user.getId(), itemId);
    }

    @PostMapping("/merge")
    public Map<String, Object> merge(@AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody MergeCartRequest req) {
        return service.mergeCart(user.getId(), req.items());
    }
}
