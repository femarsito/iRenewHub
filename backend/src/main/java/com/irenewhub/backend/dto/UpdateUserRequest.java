package com.irenewhub.backend.dto;

// ---- DTO: UPDATE USER REQUEST ----
// JSON que llega cuando el usuario edita su perfil desde "Mis Datos".
// Solo permite cambiar nombre y apellido: el email es el identificador unico y no
// se puede cambiar, y la contrasena tiene su propio flujo (forgot-password).

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // genera getters, setters, equals(), hashCode() y toString()
public class UpdateUserRequest {

    @NotBlank(message = "El nombre no puede estar vacio") // rechaza null, "" y "   "
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacio")
    private String lastName;
}
