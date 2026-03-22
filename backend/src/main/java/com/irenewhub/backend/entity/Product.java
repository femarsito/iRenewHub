package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "is_oem", nullable = false)
    private Boolean isOem;

    @ElementCollection
    @CollectionTable(
            name = "product_compatibility",
            joinColumns = @JoinColumn(name = "product_id")
    )
    @Column(name = "model")
    private List<String> compatibility;

    // Especificaciones técnicas
    @Column(nullable = false)
    private String warranty;

    @Column(nullable = false)
    private String condition;

    @Column(nullable = false)
    private String manufacturer;
}