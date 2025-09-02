package com.example.e_commerce_services.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_commerce_services.domain.Order;
import com.example.e_commerce_services.dto.OrderDetailDto;
import com.example.e_commerce_services.dto.OrderListItemDto;
import com.example.e_commerce_services.exception.NotFoundException;
import com.example.e_commerce_services.repository.OrderRepository;

@Service
public class OrderQueryService {

    private final OrderRepository orderRepo;

    public OrderQueryService(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Transactional(readOnly = true)
    public List<OrderListItemDto> listMyOrders(Long userId) {
        var fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        return orderRepo.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(o -> new OrderListItemDto(
                o.getId(), o.getNumber(), o.getStatus().name(),
                o.getTotal(), o.getCreatedAt() != null ? o.getCreatedAt().format(fmt) : null
        ))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderDetailDto getMyOrder(Long userId, Long orderId) {
        Order o = orderRepo.findById(orderId)
                .filter(ord -> ord.getUser().getId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));

        var items = o.getItems().stream()
                .map(oi -> new OrderDetailDto.Item(
                oi.getSku(), oi.getTitle(), oi.getQty(), oi.getUnitPrice(), oi.getLineTotal()
        )).toList();

        var ship = new OrderDetailDto.Shipping(
                o.getShipFullName(), o.getShipPhone(), o.getShipLine1(),
                o.getShipProvince(), o.getShipDistrict(), o.getShipWard()
        );

        var createdAt = o.getCreatedAt() != null ? o.getCreatedAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;

        return new OrderDetailDto(
                o.getId(), o.getNumber(), o.getStatus().name(),
                items, ship, o.getShippingFee(), o.getSubtotal(), o.getTotal(), createdAt
        );
    }
}
