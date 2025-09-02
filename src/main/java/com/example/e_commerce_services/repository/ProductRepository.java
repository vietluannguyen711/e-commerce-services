package com.example.e_commerce_services.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.e_commerce_services.domain.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"images", "variants", "category"})
    Optional<Product> findBySlug(String slug);

    // Liệt kê sản phẩm theo category slug
    // Cách 1 (đơn giản): load categoryId trước rồi findAllByCategoryId
    List<Product> findAllByCategory_Id(Long categoryId);

    // Hoặc tự viết @Query nếu muốn lọc/keyword sau này.
}
