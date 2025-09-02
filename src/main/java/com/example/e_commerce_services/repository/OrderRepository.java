package com.example.e_commerce_services.repository;



import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.e_commerce_services.domain.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_IdOrderByCreatedAtDesc(Long userId);
    boolean existsByNumber(String number);
}

