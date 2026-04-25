package com.irenewhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// DTO para actualizar los datos del perfil del usuario autenticado.
// No incluye contraseña ni rol (esos se cambian por otros flujos).
@Data
public class UpdateUserRequest {

    @NotBlank(message = "El nombre no puede estar vacío")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacío")
    private String lastName;
}
