package com.irenewhub.backend.controller;

import com.irenewhub.backend.dto.OrderResponse;
import com.irenewhub.backend.entity.User;
import com.irenewhub.backend.exception.ResourceNotFoundException;
import com.irenewhub.backend.repository.ProductRepository;
import com.irenewhub.backend.repository.UserRepository;
import com.irenewhub.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    // ─── ESTADÍSTICAS ─────────────────────────────────────────────────────────

    @GetMapping("/users/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        return ResponseEntity.ok(Map.of("count", userRepository.count()));
    }

    // ─── GESTIÓN DE PEDIDOS ───────────────────────────────────────────────────

    // Todos los pedidos del sistema (para el admin)
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    // Cambia el estado de un pedido (body: {"status": "SHIPPED"})
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, body.get("status")));
    }

    // Elimina un pedido (solo si está DELIVERED o CANCELLED)
    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    // ─── GESTIÓN DE USUARIOS ──────────────────────────────────────────────────

    // Lista todos los usuarios con nombre, email y rol
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getUsers() {
        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .map(u -> Map.of(
                        "id",        (Object) u.getId(),
                        "firstName", u.getFirstName(),
                        "lastName",  u.getLastName(),
                        "email",     u.getEmail(),
                        "role",      u.getRole().name()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    // Elimina un usuario por ID
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario no encontrado: " + id);
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ─── GESTIÓN DE CATEGORÍAS ────────────────────────────────────────────────

    // Categorías únicas derivadas de los productos existentes
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(productRepository.findAllDistinctCategories());
    }

    // Renombra una categoría en todos los productos que la usan
    // Body: {"oldName": "Pantallas", "newName": "Pantallas LCD"}
    @Transactional
    @PutMapping("/categories/rename")
    public ResponseEntity<Map<String, Object>> renameCategory(
            @RequestBody Map<String, String> body) {
        String oldName = body.get("oldName");
        String newName = body.get("newName");
        int updated = productRepository.updateCategoryName(oldName, newName);
        return ResponseEntity.ok(Map.of("updated", updated, "message", "Categoría renombrada"));
    }

    // Elimina una categoría — solo si ningún producto la usa
    @Transactional
    @DeleteMapping("/categories/{name}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable String name) {
        long count = productRepository.countByCategory(name);
        if (count > 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La categoría tiene " + count + " productos. Reasígnalos antes de eliminarla."));
        }
        return ResponseEntity.ok(Map.of("message", "Categoría eliminada"));
    }
}
