package com.irenewhub.backend.dto;

// ─── DTO: FORGOT PASSWORD REQUEST ────────────────────────────────────────────
// JSON que llega al endpoint POST /api/auth/forgot-password.
// El usuario solo necesita introducir su email para iniciar el proceso.

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequest {

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;
}
