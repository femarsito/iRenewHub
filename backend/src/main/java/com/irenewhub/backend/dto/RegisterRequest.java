package com.irenewhub.backend.dto;

// ---- DTO: REGISTER REQUEST ----
// JSON que llega del frontend cuando un usuario se registra.
// Solo contiene los campos necesarios para crear la cuenta, nada mas.
// Las anotaciones de validacion hacen que Spring rechace el JSON si los datos son incorrectos.

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio") // rechaza null, "" y "   " (espacios)
    private String firstName;

    @NotBlank(message = "El apellido es obligatorio") // rechaza null, "" y "   "
    private String lastName;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email invalido") // valida que tenga formato correcto: x@x.x
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") // minimo de seguridad
    private String password;
}
