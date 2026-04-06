/* Explicacion de Entity
* Este<paquete sirve para mapear a las tablas SQL
* y en este caso esta clase mapea la tabla de Productos */


package com.irenewhub.backend.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal; //mejor usar este para los precios, para evitar errores de precisión
import java.util.List;

@Entity //le dice a JPA que esta clase es una tabla de la BBDD
@Table(name = "products") //nombre de la tabla de PostgreSQL
@Data //genera automaicamente los getters, getters, equals(), hashcode() y toString()
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    //id (PK) generada automaticamente por PostgreSQL con un autoincrement
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //
    @Column(nullable = false) //NOT NULL
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    //ColumnDefinition fuerza el tipo TEXT en vez de VARCHAR
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "is_oem", nullable = false)
    private Boolean isOem;

    // Esto es para la lista de compatibilidades
    // ["iPhone 14 Pro, iPhone 14"]
    @ElementCollection(fetch = FetchType.EAGER)
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