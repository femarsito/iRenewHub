package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.*;

// ---- ENTIDAD COMMENT LIKE ----
// Representa la tabla "comment_likes" en PostgreSQL.
// Cada fila significa que un usuario concreto ha dado like a un comentario concreto.
//
// Por que existe esta tabla y no se guarda solo un numero en Comment?
// Porque con solo el numero no sabemos quien dio like, y un usuario podria
// dar like infinitas veces. Con esta tabla, la combinacion (commentId + userEmail)
// solo puede existir UNA vez gracias al UniqueConstraint.
//
// Ejemplo de filas:
//   id | comment_id | user_email
//    1 |     5      | juan@gmail.com     <- juan dio like al comentario 5
//    2 |     5      | maria@gmail.com    <- maria dio like al comentario 5
//    3 |     8      | juan@gmail.com     <- juan dio like al comentario 8
// El comentario 5 tiene 2 likes, el 8 tiene 1.

@Entity
@Table(name = "comment_likes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_email"})) // evita que el mismo usuario de like dos veces al mismo comentario
@Data
@Builder
@NoArgsConstructor // constructor vacio, necesario para JPA
@AllArgsConstructor // constructor con todos los campos, necesario para @Builder
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement
    private Long id;

    @Column(name = "comment_id", nullable = false) // id del comentario al que se dio like
    private Long commentId;

    @Column(name = "user_email", nullable = false) // email del usuario que dio el like
    private String userEmail;
}
