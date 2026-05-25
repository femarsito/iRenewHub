package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity                            // Hibernate crea/gestiona la tabla "categories"
@Table(name = "categories")        // Nombre de la tabla en la BD
@Data                              // Getters, setters, equals, hashCode, toString
@NoArgsConstructor                 // Constructor vacio requerido por JPA
@AllArgsConstructor                // Constructor con todos los campos (para @Builder)
@Builder                           // Category.builder().name("Baterias").build()
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento en PostgreSQL
    private Long id;

    @Column(unique = true, nullable = false) // No puede haber dos categorias con el mismo nombre
    private String name;
}
