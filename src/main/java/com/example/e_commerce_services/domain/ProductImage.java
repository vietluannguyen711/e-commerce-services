package com.example.e_commerce_services.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "product_images")
@Data
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(nullable = false)
    private Integer position = 0;

    @Column(nullable = false)
    private String fileName;  // uuid.jpg
    @Column(nullable = false)
    private boolean isMain = false;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
    // getters/setters ...
}
