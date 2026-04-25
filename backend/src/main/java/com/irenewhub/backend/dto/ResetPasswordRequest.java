package com.irenewhub.backend.dto;

// ─── DTO: RESET PASSWORD REQUEST ─────────────────────────────────────────────
// JSON que llega al endpoint POST /api/auth/reset-password.
// El usuario introduce el token que recibió (simulado en demo) y su nueva contraseña.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "El token es obligatorio")
    private String token;        // Token generado en /forgot-password

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String newPassword;
}
