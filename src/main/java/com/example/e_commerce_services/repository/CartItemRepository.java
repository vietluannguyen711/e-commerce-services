package com.example.e_commerce_services.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.e_commerce_services.domain.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCart_Id(Long id, Long cartId);

    Optional<CartItem> findByCart_IdAndVariant_Id(Long cartId, Long variantId);

    void deleteByCart_IdAndId(Long cartId, Long id);
}
