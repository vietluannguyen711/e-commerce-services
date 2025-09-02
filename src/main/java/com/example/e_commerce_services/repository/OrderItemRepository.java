package com.example.e_commerce_services.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.example.e_commerce_services.domain.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}

