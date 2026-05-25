package com.irenewhub.backend.dto;

// ---- DTO: FORGOT PASSWORD REQUEST ----
// JSON que llega al endpoint POST /api/auth/forgot-password.
// El usuario solo necesita su email: el backend busca la cuenta y envia el enlace de recuperacion.

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // genera getters, setters, equals(), hashCode() y toString()
public class ForgotPasswordRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email invalido") // valida que tenga formato correcto: x@x.x
    private String email;
}
