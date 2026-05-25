package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE USUARIOS ----
// Interfaz que gestiona todas las consultas a la tabla users.
// Al extender JpaRepository, Spring genera automaticamente la implementacion
// en tiempo de arranque: no hay que escribir ningun metodo SQL manualmente.
// JpaRepository<User, Long>:
//   - User  → la entidad con la que trabaja (la tabla users)
//   - Long  → el tipo del campo @Id de esa entidad

import com.irenewhub.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // marca esta interfaz como componente de Spring para que pueda inyectarse con @Autowired en los servicios
public interface UserRepository extends JpaRepository<User, Long> {

    // Spring lee "findByEmail" y genera: SELECT * FROM users WHERE email = ?
    // Devuelve Optional porque el usuario puede no existir: evita NullPointerException
    Optional<User> findByEmail(String email);

    // Spring lee "existsByEmail" y genera: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    // Devuelve boolean directamente: true si ya existe ese email, false si no
    // Se usa en el registro para evitar cuentas duplicadas
    boolean existsByEmail(String email);

    // Spring lee "findByResetToken" y genera: SELECT * FROM users WHERE reset_token = ?
    // Se usa en reset-password para buscar al usuario que tiene ese token de recuperacion
    // Devuelve Optional porque si el token no existe o ya expiro no habra resultado
    Optional<User> findByResetToken(String resetToken);
}
