package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ---- ENTIDAD COMMENT ----
// Representa la tabla "comments" en PostgreSQL.
// Guarda los comentarios que los usuarios escriben en la pagina de cada producto.
// Soporta respuestas anidadas: un comentario puede responder a otro (parentId).
//
// Flujo de cupones por likes:
//   1. Un comentario recibe 5 likes
//   2. CommentService lo detecta (totalLikes >= 5 y couponGranted == false)
//   3. Genera un cupon del 5% para el autor del comentario
//   4. Marca couponGranted = true para no generar mas cupones por ese comentario

@Entity
@Table(name = "comments")
@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
@Builder
@NoArgsConstructor // constructor vacio, necesario para JPA
@AllArgsConstructor // constructor con todos los campos, necesario para @Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement
    private Long id;

    @Column(name = "product_id", nullable = false) // id del producto al que pertenece este comentario
    private Long productId;

    @Column(name = "user_email", nullable = false) // email del autor, se usa para enviarle el cupon si llega a 5 likes
    private String userEmail;

    @Column(name = "user_name", nullable = false) // nombre visible del autor (firstName + lastName)
    private String userName;

    @Column(nullable = false, columnDefinition = "TEXT") // TEXT = sin limite de caracteres
    private String content;

    @Column(name = "parent_id") // null = comentario raiz | numero = respuesta al comentario con ese id
    private Long parentId;

    @Builder.Default // sin esto el builder pondria null en vez de 0
    @Column(nullable = false)
    private int likes = 0; // numero de likes, se actualiza cada vez que alguien da o quita like. Pero el valor inicial es 0

    @Builder.Default // sin esto el builder pondria null en vez de false
    @Column(name = "coupon_granted", nullable = false)
    private boolean couponGranted = false; // false = no se ha generado cupon | true = ya se genero, no se generara otro

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist // se ejecuta automaticamente ANTES del primer INSERT, JPA lo llama solo
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
