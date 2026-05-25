package com.irenewhub.backend.dto;

// ---- DTO: RESET PASSWORD REQUEST ----
// JSON que llega al endpoint POST /api/auth/reset-password.
// El usuario llega aqui desde el enlace del email, que incluye el token en la URL.
// El frontend extrae el token de la URL y lo manda junto con la nueva contrasena.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // genera getters, setters, equals(), hashCode() y toString()
public class ResetPasswordRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token; // UUID generado en /forgot-password, guardado en la BD con 1 hora de expiracion

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String newPassword; // nueva contrasena elegida por el usuario
}
