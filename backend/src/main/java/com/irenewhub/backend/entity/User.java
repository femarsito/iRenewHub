package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ---- ENTIDAD USER ----
// Representa la tabla "users" en PostgreSQL.
// La contrasena NUNCA se guarda en texto plano, se guarda el hash BCrypt.

// ---- ANOTACIONES DE CLASE ----
@Entity // le dice a JPA que esta clase representa una tabla en la base de datos
@Table(name = "users") // el nombre de la tabla en PostgreSQL sera "users"
@Data // genera auomaticamente los getters, setteres, equals(), toString() de forma automatica y asi me ahorro decenas de lineas

// @Builder (Lombok) -> permite crear objetos
// Lo usamos en AuthService para crear nuevos usuarios
@Builder

@NoArgsConstructor // genera un constructor vacio

@AllArgsConstructor // genera un constructor con todos los campos, es necesario para que el @Builder funcione

public class User {


    @Id // marca este campo como la clave primaria (PK) de la tabla

    @GeneratedValue(strategy = GenerationType.IDENTITY) // genera la ID  automaticamente al guardarse en la BBDD y el strategy es practicamente un AUTOINCREMENT
    private Long id;

    @Column(nullable = false, unique = true) // el email es OBLIGATORIO y UNICO, un correo no se debe de repetir en la BBD
    private String email;

    @Column(nullable = false) // campo OBLIGATORIO, aqui se guarda el hash BCrypt, NUNCA la contraseña real
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING) //guarda el ENUM como TEXTO en la BDD
    @Column(nullable = false) //Tiene que ser si o si un Usuario o Admin
    private Role role;

    @Column(name = "reset_token") //no se nullable porque es OPCIONAL, cuando se resetea la contraseña correctamente el Authservice lo vuelve a poner null
    private String resetToken;         // UUID unico generado al pedir recuperacion

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;  // Expira 1 hora despues de generarse

    public enum Role {     // Enum = lista fija de valores posibles. Solo puede ser USER o ADMIN.
        USER, ADMIN
    }
}
