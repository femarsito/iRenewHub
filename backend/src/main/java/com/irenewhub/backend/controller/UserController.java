package com.irenewhub.backend.controller;

// ─── CONTROLADOR DE PERFIL DE USUARIO ────────────────────────────────────────
// PUT /api/users/me → actualiza nombre y apellido del usuario autenticado.
// @Transactional garantiza que el findByEmail() y el save() comparten la misma
// sesión JPA, evitando que la entidad quede "detached" y cause un INSERT en vez
// de un UPDATE.

import com.irenewhub.backend.dto.UpdateUserRequest;
import com.irenewhub.backend.entity.User;
import com.irenewhub.backend.exception.ResourceNotFoundException;
import com.irenewhub.backend.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @Transactional
    @PutMapping("/api/users/me")
    public ResponseEntity<Map<String, String>> updateMe(
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Modificar la entidad gestionada por JPA → Hibernate detecta el cambio
        // y genera UPDATE automáticamente al final de la transacción
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // save() con entity que tiene ID → siempre UPDATE, nunca INSERT
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "firstName", user.getFirstName(),
                "lastName",  user.getLastName(),
                "message",   "Datos actualizados correctamente"
        ));
    }
}
