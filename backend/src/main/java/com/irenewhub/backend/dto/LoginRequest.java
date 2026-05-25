package com.irenewhub.backend.dto;

// ---- DTO: LOGIN REQUEST ----
// JSON que llega del frontend cuando un usuario inicia sesion.
// El backend lo recibe, comprueba las credenciales y devuelve un AuthResponse con el JWT.

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
public class LoginRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email invalido") // valida que tenga formato correcto: x@x.x
    private String email;

    @NotBlank(message = "La contrasena es obligatoria") // rechaza null, "" y "   " (espacios)
    private String password;
}



