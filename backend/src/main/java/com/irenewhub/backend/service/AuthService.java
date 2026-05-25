package com.irenewhub.backend.service;

// Gestiona registro, login y recuperación de contraseña.
// La recuperación funciona en 2 pasos: forgotPassword genera un token y lo envía por email,
// resetPassword lo valida y actualiza la contraseña.

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
    private final EmailService emailService;

    // ---- REGISTRO ----
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

    // ---- LOGIN ----
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

    // ---- RECUPERAR CONTRASEÑA (PASO 1) ----
    // Genera un token UUID, lo guarda con expiración de 1 hora y envía el email.
    public Map<String, String> forgotPassword(String email) {

        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No existe cuenta con ese email"));

        // Generar un token único (ej: "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        String token = UUID.randomUUID().toString();

        // Guardar token y expiración en el usuario
        usuario.setResetToken(token);
        usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(usuario);

        // Enviar email con el enlace de recuperación
        emailService.enviarEmailRecuperacion(email, token);

        // Devolvemos solo un mensaje genérico — el token nunca viaja al frontend
        return Map.of("message", "Hemos enviado un enlace de recuperación a tu email.");
    }

    // ---- CAMBIAR CONTRASEÑA (PASO 2) ----
    // Valida el token recibido y actualiza la contraseña si no ha expirado.
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