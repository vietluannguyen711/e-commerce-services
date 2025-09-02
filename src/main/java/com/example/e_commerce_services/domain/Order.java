package com.example.e_commerce_services.domain;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, unique = true, length = 40)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PLACED;

    // Shipping snapshot
    @Column(name = "ship_full_name", nullable = false, length = 120)
    private String shipFullName;
    @Column(name = "ship_phone", nullable = false, length = 30)
    private String shipPhone;
    @Column(name = "ship_line1", nullable = false, length = 255)
    private String shipLine1;
    @Column(name = "ship_province", nullable = false, length = 120)
    private String shipProvince;
    @Column(name = "ship_district", nullable = false, length = 120)
    private String shipDistrict;
    @Column(name = "ship_ward", nullable = false, length = 120)
    private String shipWard;

    @Column(name = "shipping_fee", nullable = false)
    private Integer shippingFee = 0;
    @Column(nullable = false)
    private Integer subtotal;
    @Column(nullable = false)
    private Integer total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void pp() {
        createdAt = updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    void pu() {
        updatedAt = OffsetDateTime.now();
    }

    // getters/setters ...
}
