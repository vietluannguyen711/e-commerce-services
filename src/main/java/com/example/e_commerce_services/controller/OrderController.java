package com.example.e_commerce_services.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_commerce_services.dto.OrderDetailDto;
import com.example.e_commerce_services.dto.OrderListItemDto;
import com.example.e_commerce_services.dto.PlaceOrderRequest;
import com.example.e_commerce_services.dto.PreviewResponse;
import com.example.e_commerce_services.dto.ShippingAddressDto;
import com.example.e_commerce_services.security.UserPrincipal;
import com.example.e_commerce_services.service.CheckoutService;
import com.example.e_commerce_services.service.OrderQueryService;

import jakarta.validation.Valid;

@RestController
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderQueryService orderQueryService;

    public OrderController(CheckoutService checkoutService, OrderQueryService orderQueryService) {
        this.checkoutService = checkoutService;
        this.orderQueryService = orderQueryService;
    }

    // POST /checkout/preview
    @PostMapping("/checkout/preview")
    public PreviewResponse preview(@AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ShippingAddressDto address) {
        return checkoutService.preview(user.getId(), address);
    }

    // POST /orders
    @PostMapping("/orders")
    public Map<String, Object> placeOrder(@AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody PlaceOrderRequest req) {
        var saved = checkoutService.placeOrder(user.getId(), req);
        return Map.of(
                "id", saved.getId(),
                "status", saved.getStatus().name(),
                "number", saved.getNumber(),
                "total", saved.getTotal(),
                "createdAt", saved.getCreatedAt()
        );
    }

    // GET /orders/me
    @GetMapping("/orders/me")
    public List<OrderListItemDto> myOrders(@AuthenticationPrincipal UserPrincipal user) {
        return orderQueryService.listMyOrders(user.getId());
    }

    // GET /orders/{id}
    @GetMapping("/orders/{id}")
    public OrderDetailDto myOrder(@AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long id) {
        return orderQueryService.getMyOrder(user.getId(), id);
    }
}
