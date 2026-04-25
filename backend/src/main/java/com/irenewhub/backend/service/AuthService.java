package com.irenewhub.backend.service;

// ─── SERVICIO DE AUTENTICACIÓN ────────────────────────────────────────────────
// Gestiona el registro, el login y la recuperación de contraseña.
//
// Flujo de recuperación de contraseña (2 pasos):
//   1. forgotPassword(email)   → genera un token, lo guarda en el usuario y lo devuelve
//   2. [Demo] El frontend muestra el token al usuario (en prod. se enviaría por email)
//   3. resetPassword(token, newPassword) → valida el token y cambia la contraseña

import com.irenewhub.backend.dto.AuthResponse;
import com.irenewhub.backend.dto.LoginRequest;
import com.irenewhub.backend.dto.RegisterRequest;
import com.irenewhub.backend.entity.User;
import com.irenewhub.backend.exception.ResourceNotFoundException;
import com.irenewhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // ─── REGISTRO ─────────────────────────────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {

        // 1. Comprobar que el email no está ya registrado
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ya existe una cuenta con ese email");
        }

        // 2. Crear el usuario con la contraseña hasheada (nunca texto plano)
        User nuevoUsuario = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(User.Role.USER)  // Todo nuevo usuario empieza como USER
                .build();

        userRepository.save(nuevoUsuario);

        // 3. Generar JWT y devolver respuesta
        String token = jwtService.generateToken(nuevoUsuario.getEmail());
        return construirRespuesta(nuevoUsuario, token);
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    public AuthResponse login(LoginRequest request) {

        // 1. Buscar el usuario por email (lanza 404 si no existe)
        User usuario = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No existe cuenta con ese email"));

        // 2. Comparar la contraseña con el hash guardado en la BBDD
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // 3. Generar JWT y devolver respuesta
        String token = jwtService.generateToken(usuario.getEmail());
        return construirRespuesta(usuario, token);
    }

    // ─── RECUPERAR CONTRASEÑA (PASO 1) ────────────────────────────────────────
    // Genera un token único, lo guarda en el usuario con una expiración de 1 hora
    // y lo devuelve en la respuesta.
    //
    // NOTA DEMO: En una aplicación real este token se enviaría por email
    // (con JavaMailSender o similar). Para el TFG, lo devolvemos directamente
    // en la respuesta para poder demostrar el flujo completo sin servidor de correo.
    public Map<String, String> forgotPassword(String email) {

        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No existe cuenta con ese email"));

        // Generar un token único con UUID (ej: "a1b2c3d4-e5f6-...")
        String token = UUID.randomUUID().toString();

        // Guardar el token y su fecha de expiración en el usuario
        usuario.setResetToken(token);
        usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));  // Expira en 1 hora
        userRepository.save(usuario);

        // Devolver el token para que el frontend lo muestre al usuario
        // (en producción: enviar por email y devolver solo un mensaje genérico)
        return Map.of(
                "token", token,
                "message", "Token generado. En producción se enviaría por email."
        );
    }

    // ─── CAMBIAR CONTRASEÑA (PASO 2) ──────────────────────────────────────────
    // Recibe el token y la nueva contraseña. Valida el token y actualiza la contraseña.
    public Map<String, String> resetPassword(String token, String newPassword) {

        // 1. Buscar el usuario que tenga ese token
        User usuario = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o ya utilizado"));

        // 2. Comprobar que el token no ha expirado
        if (usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha expirado. Solicita uno nuevo.");
        }

        // 3. Hashear la nueva contraseña y guardarla
        usuario.setPassword(passwordEncoder.encode(newPassword));

        // 4. Limpiar el token para que no pueda reutilizarse
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);

        userRepository.save(usuario);

        return Map.of("message", "Contraseña actualizada correctamente. Ya puedes iniciar sesión.");
    }

    // ─── MÉTODO AUXILIAR ──────────────────────────────────────────────────────
    // Construye el AuthResponse con el JWT y los datos del usuario.
    private AuthResponse construirRespuesta(User usuario, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(usuario.getEmail())
                .firstName(usuario.getFirstName())
                .lastName(usuario.getLastName())
                .role(usuario.getRole().name())  // "USER" o "ADMIN"
                .build();
    }
}