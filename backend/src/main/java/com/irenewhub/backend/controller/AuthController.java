package com.irenewhub.backend.controller;

import com.irenewhub.backend.dto.AuthResponse;
import com.irenewhub.backend.dto.ForgotPasswordRequest;
import com.irenewhub.backend.dto.LoginRequest;
import com.irenewhub.backend.dto.RegisterRequest;
import com.irenewhub.backend.dto.ResetPasswordRequest;
import com.irenewhub.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // ---- RECUPERACIÓN DE CONTRASEÑA (PASO 1) ----
    // Recibe el email, genera el token y lo devuelve en la respuesta.
    // DEMO: en producción este token se enviaría por email, no en la respuesta HTTP.
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request.getEmail()));
    }

    // ---- RECUPERACIÓN DE CONTRASEÑA (PASO 2) ----
    // Recibe el token (que el usuario copió del paso 1) y la nueva contraseña.
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request.getToken(), request.getNewPassword()));
    }
}