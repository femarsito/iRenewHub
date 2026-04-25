package com.irenewhub.backend.repository;

import com.irenewhub.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserEmailOrderByCreatedAtDesc(String email);

    // Todos los pedidos ordenados por fecha más reciente (para el panel admin)
    List<Order> findAllByOrderByCreatedAtDesc();
}