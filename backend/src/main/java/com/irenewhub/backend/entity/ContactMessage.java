package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ---- ENTIDAD CONTACT MESSAGE ----
// Representa la tabla "contact_messages" en PostgreSQL.
// Guarda los mensajes enviados desde el formulario de contacto.
// El administrador puede verlos en el panel de admin y cambiar su estado.

@Entity
@Table(name = "contact_messages")
@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
@Builder
@NoArgsConstructor // constructor vacio, necesario para JPA
@AllArgsConstructor // constructor con todos los campos, necesario para @Builder
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement
    private Long id;

    @Column(nullable = false) // nombre del remitente (no tiene que ser un usuario registrado)
    private String name;

    @Column(nullable = false) // email del remitente para poder responderle
    private String email;

    private String phone; // telefono opcional, sin nullable = false → puede ser null

    @Column(nullable = false) // asunto del mensaje
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT") // texto del mensaje, TEXT = sin limite de caracteres
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING) // guarda el estado como texto: "UNREAD" o "REPLIED"
    @Column(nullable = false)
    private MessageStatus status;

    @PrePersist // se ejecuta automaticamente ANTES del primer INSERT, JPA lo llama solo
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = MessageStatus.UNREAD; // todoo mensaje nuevo empieza como pendiente
        }
    }

    public enum MessageStatus { // solo dos estados: pendiente o respondido
        UNREAD,  // pendiente: el admin aun no ha respondido al usuario
        REPLIED  // respondido: el admin ya gestiono el mensaje
    }
}
