package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// ---- ENTIDAD COMMENT REPORT ----
// Representa la tabla "comment_reports" en PostgreSQL.
// Cuando un usuario denuncia un comentario inapropiado, se crea una fila aqui.
// El administrador puede ver las denuncias en el panel de admin y resolverlas.
// Se guarda una copia del texto del comentario (commentContent) en el momento
// de la denuncia, por si el comentario se borra despues.

@Entity
@Table(name = "comment_reports")
@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
@Builder
@NoArgsConstructor // constructor vacio, necesario para JPA
@AllArgsConstructor // constructor con todos los campos, necesario para @Builder
public class CommentReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement
    private Long id;

    @Column(name = "comment_id", nullable = false) // id del comentario denunciado
    private Long commentId;

    @Column(name = "comment_content", nullable = false, columnDefinition = "TEXT") // copia del texto en el momento de la denuncia
    private String commentContent;

    @Column(name = "reporter_email", nullable = false) // email del usuario que denuncia
    private String reporterEmail;

    @Column(name = "reporter_name", nullable = false) // nombre del usuario que denuncia
    private String reporterName;

    @Column(name = "reported_user_email", nullable = false) // email del autor del comentario denunciado
    private String reportedUserEmail;

    @Column(name = "reported_user_name", nullable = false) // nombre del autor del comentario denunciado
    private String reportedUserName;

    @Column(nullable = false) // motivo de la denuncia escrito por el denunciante
    private String reason;

    @Enumerated(EnumType.STRING) // guarda el estado como texto: "PENDING", "IGNORED", "RESOLVED"
    @Builder.Default // sin esto el builder pondria null en vez de PENDING
    @Column(nullable = false)
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist // se ejecuta automaticamente ANTES del primer INSERT, JPA lo llama solo
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    public enum ReportStatus { // estados posibles de la denuncia
        PENDING,  // nueva, el admin no la ha revisado todavia
        IGNORED,  // el admin la reviso y decidio no actuar
        RESOLVED  // el admin actuo (por ejemplo borrando el comentario)
    }
}
