package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// ---- ENTIDAD PRODUCT ----
// Representa la tabla "products" en PostgreSQL.

@Entity // esta clase es una tabla en la BD
@Table(name = "products") // nombre de la tabla en PostgreSQL
@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
@Builder // permite crear objetos con Product.builder().name("...").price(...).build()
@NoArgsConstructor // constructor vacio, necesario para que JPA pueda crear objetos
@AllArgsConstructor // constructor con todos los campos, necesario para que @Builder funcione
public class Product {

    @Id // clave primaria de la tabla
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement, PostgreSQL asigna el id solo
    private Long id;

    @Column(nullable = false) // NOT NULL, el nombre es obligatorio
    private String name;

    @Column(nullable = false) // categoria del producto: "Bateria", "Pantalla", etc.
    private String category;

    @Column(nullable = false, precision = 10, scale = 2) // BigDecimal para dinero: evita errores de precision de double
    private BigDecimal price; // precision=10 → max 10 digitos, scale=2 -> 2 decimales (ej: 49.99)

    @Column(name = "original_price", precision = 10, scale = 2) // precio antes del descuento, puede ser null si no hay descuento
    private BigDecimal originalPrice;

    @Column(nullable = false, columnDefinition = "TEXT") // TEXT = sin limite de caracteres (VARCHAR tiene limite de 255)
    private String description;

    @Column(name = "image_url") // ruta de la imagen, puede ser null
    private String imageUrl;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "is_oem", nullable = false) // true = pieza original OEM, false = pieza compatible
    private Boolean isOem;

    @Column(nullable = false) // garantia del producto, ej: "12 meses"
    private String warranty;

    @Column(nullable = false) // estado del producto, ej: "Nuevo", "Reacondicionado"
    private String condition;

    @Column(nullable = false) // fabricante del repuesto, ej: "Apple OEM"
    private String manufacturer;
}
