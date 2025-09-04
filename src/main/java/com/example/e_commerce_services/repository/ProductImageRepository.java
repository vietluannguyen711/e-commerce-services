package com.example.e_commerce_services.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.e_commerce_services.domain.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByIsMainDescCreatedAtDesc(Long productId);

    @Modifying
    @Query("update ProductImage pi set pi.isMain=false where pi.product.id=:pid and pi.id<>:keepId")
    void unsetOthersExcept(@Param("pid") Long productId, @Param("keepId") Long keepId);
}
