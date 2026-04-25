package com.irenewhub.backend.repository;

// ─── REPOSITORIO DE USUARIOS ──────────────────────────────────────────────────
// Spring Data JPA genera el SQL automáticamente leyendo el nombre del método.

import com.irenewhub.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // SELECT COUNT(*) > 0 FROM users WHERE email = ? → para evitar emails duplicados al registrar
    boolean existsByEmail(String email);

    // SELECT * FROM users WHERE reset_token = ?
    // Se usa al validar el token de recuperación de contraseña
    Optional<User> findByResetToken(String resetToken);
}
