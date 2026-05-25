package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ---- ENTIDAD COUPON ----
// Representa la tabla "coupons" en PostgreSQL.
// Un cupon se genera automaticamente cuando un comentario llega a 5 likes.
// Solo el autor del comentario recibe el cupon (userEmail).
// Da un descuento del 5% en la proxima compra y solo puede usarse una vez.
//
// Flujo completo:
//   1. CommentService detecta 5 likes -> genera codigo "IRH-XXXXXXXX"
//   2. Guarda el cupon con used = false
//   3. El usuario lo ve en "Mis Cupones" y lo introduce en el carrito
//   4. Al confirmar el pedido, OrderService pone used = true

@Entity
@Table(name = "coupons")
@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
@Builder
@NoArgsConstructor // constructor vacio, necesario para JPA
@AllArgsConstructor // constructor con todos los campos, necesario para @Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement
    private Long id;

    @Column(name = "user_email", nullable = false) // email del usuario al que pertenece, solo el puede canjearlo
    private String userEmail;

    @Column(nullable = false, unique = true) // codigo del cupon, ej: "IRH-A1B2C3D4" | unique = no pueden existir dos iguales
    private String code;

    @Builder.Default // sin esto el builder pondria null en vez de 5
    @Column(name = "discount_percent", nullable = false)
    private int discountPercent = 5; // porcentaje de descuento, por defecto 5%

    @Builder.Default // sin esto el builder pondria null en vez de false
    @Column(nullable = false)
    private boolean used = false; // false = disponible | true = ya canjeado, no se puede volver a usar

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist // se ejecuta automaticamente ANTES del primer INSERT, JPA lo llama solo
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
