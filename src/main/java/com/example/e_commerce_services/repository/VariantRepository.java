package com.example.e_commerce_services.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.e_commerce_services.domain.Variant;

public interface VariantRepository extends JpaRepository<Variant, Long> {

    Optional<Variant> findBySku(String sku);

    // Check availability theo productSlug + attrs JSONB (@> chứa)
    @Query(value = """
            select v.* from variants v
            join products p on p.id = v.product_id
            where p.slug = :productSlug
              and v.attrs @> cast(:attrs as jsonb)
            limit 1
            """, nativeQuery = true)
    Optional<Variant> findByProductSlugAndAttrs(String productSlug, String attrs);
}
