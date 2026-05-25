package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE CUPONES ----
// Interfaz que gestiona las consultas a la tabla coupons.
// Los cupones pertenecen a un usuario concreto y tienen un codigo unico.
// Un cupon puede estar usado (used = true) o disponible (used = false).

import com.irenewhub.backend.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository // registra esta interfaz como componente de Spring para inyectarla con @Autowired
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // Spring genera: SELECT * FROM coupons WHERE user_email = ? ORDER BY created_at DESC
    List<Coupon> findByUserEmailOrderByCreatedAtDesc(String userEmail);    // Se usa en /cupones para mostrar al usuario sus cupones disponibles y usados


    // Spring lee: findBy Code And UserEmail And UsedFalse
    // Genera: SELECT * FROM coupons WHERE code = ? AND user_email = ? AND used = false
    // UsedFalse → Spring entiende que el campo used debe ser false (cupon sin usar)
    // Devuelve Optional porque si el codigo no existe o ya esta usado no hay resultado
    Optional<Coupon> findByCodeAndUserEmailAndUsedFalse(String code, String userEmail);    // Se usa al aplicar un cupon en el checkout para validarlo antes de descontarlo

}
