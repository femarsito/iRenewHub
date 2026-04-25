package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ─── ENTIDAD USER ─────────────────────────────────────────────────────────────
// Mapea la tabla "users" de PostgreSQL.
// Almacena las credenciales, el rol y, opcionalmente, el token de recuperación
// de contraseña generado cuando el usuario pulsa "Olvidé mi contraseña".
// La contraseña NUNCA se guarda en texto plano → BCrypt la hashea antes de guardar.
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true → no pueden existir dos usuarios con el mismo email
    @Column(nullable = false, unique = true)
    private String email;

    // Aquí se guarda el hash BCrypt, no la contraseña real
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    // ─── ROL ──────────────────────────────────────────────────────────────────
    // @Enumerated(EnumType.STRING) guarda "USER" o "ADMIN" en texto,
    // no como número (0/1), así la BBDD es más legible.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ─── RECUPERACIÓN DE CONTRASEÑA ───────────────────────────────────────────
    // Estos dos campos son nullable (sin @Column nullable=false) porque
    // solo tienen valor cuando el usuario ha pedido recuperar su contraseña.
    // Una vez que resetea la contraseña correctamente, se vuelven a poner a null.

    @Column(name = "reset_token")
    private String resetToken;          // Token único generado al pedir recuperación

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;  // El token expira 1 hora después de generarse

    // Los dos roles del sistema:
    // USER  → cliente normal, puede comprar
    // ADMIN → puede crear, editar y borrar productos
    public enum Role {
        USER, ADMIN
    }
}
