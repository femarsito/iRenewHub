package com.irenewhub.backend.dto;

// ---- DTO: AUTH RESPONSE ----
// JSON que el backend devuelve al frontend despues de un login o registro exitoso.
// El frontend guarda el token en sessionStorage y lo manda en cada peticion siguiente.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data           // genera getters, setters, equals(), hashCode() y toString()
@Builder        // permite construir la respuesta con AuthResponse.builder().token("...").build()
@NoArgsConstructor // constructor vacio requerido por Jackson para deserializar JSON
@AllArgsConstructor // constructor con todos los campos, necesario para que @Builder funcione
public class AuthResponse {

    private String token;     // JWT firmado que el frontend usara para autenticarse en cada peticion
    private String email;     // email del usuario, se guarda en sessionStorage
    private String firstName; // nombre, se usa en la navbar para mostrar "Hola, Fernando"
    private String lastName;  // apellido
    private String role;      // "USER" o "ADMIN", el frontend lo usa para redirigir a /home o /admin
}
