package com.irenewhub.backend.controller;

import com.irenewhub.backend.dto.OrderResponse;
import com.irenewhub.backend.entity.Category;
import com.irenewhub.backend.entity.ContactMessage;
import com.irenewhub.backend.entity.User;
import com.irenewhub.backend.exception.ResourceNotFoundException;
import com.irenewhub.backend.repository.CategoryRepository;
import com.irenewhub.backend.repository.ContactRepository;
import com.irenewhub.backend.repository.ProductRepository;
import com.irenewhub.backend.repository.UserRepository;
import com.irenewhub.backend.service.CommentService;
import com.irenewhub.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final OrderService orderService;
    private final CommentService commentService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ContactRepository contactRepository;
    private final CategoryRepository categoryRepository;

    // ---- ESTADÍSTICAS ----

    @GetMapping("/users/count")
    public ResponseEntity<Map<String, Long>> getUserCount() {
        return ResponseEntity.ok(Map.of("count", userRepository.count()));
    }

    // ---- GESTIÓN DE PEDIDOS ----
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

    // ---- GESTIÓN DE USUARIOS ----

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

    // ---- MENSAJES DE CONTACTO ----

    @GetMapping("/messages")
    public ResponseEntity<List<Map<String, Object>>> getMessages() {
        List<Map<String, Object>> messages = contactRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(m -> Map.of(
                        "id",        (Object) m.getId(),
                        "name",      m.getName(),
                        "email",     m.getEmail(),
                        "subject",   m.getSubject(),
                        "message",   m.getMessage(),
                        "status",    m.getStatus().name(),
                        "createdAt", m.getCreatedAt().toString()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(messages);
    }

    @PutMapping("/messages/{id}/status")
    public ResponseEntity<Map<String, Object>> toggleMessageStatus(@PathVariable Long id) {
        ContactMessage msg = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mensaje no encontrado: " + id));
        msg.setStatus(msg.getStatus() == ContactMessage.MessageStatus.REPLIED
                ? ContactMessage.MessageStatus.UNREAD
                : ContactMessage.MessageStatus.REPLIED);
        contactRepository.save(msg);
        return ResponseEntity.ok(Map.of("id", (Object) msg.getId(), "status", msg.getStatus().name()));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        if (!contactRepository.existsById(id)) {
            throw new ResourceNotFoundException("Mensaje no encontrado: " + id);
        }
        contactRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- GESTIÓN DE CATEGORÍAS ----

    // Devuelve la union de categorias de productos + categorias creadas manualmente
    // TreeSet ordena y elimina duplicados automaticamente
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        Set<String> all = new TreeSet<>();
        all.addAll(productRepository.findAllDistinctCategories());
        categoryRepository.findAll().forEach(c -> all.add(c.getName()));
        return ResponseEntity.ok(new ArrayList<>(all));
    }

    // Crea una categoria nueva y la persiste en la tabla categories
    @PostMapping("/categories")
    public ResponseEntity<Map<String, Object>> createCategory(
            @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nombre requerido"));
        }
        if (categoryRepository.existsByName(name) || productRepository.countByCategory(name) > 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Esa categoria ya existe"));
        }
        Category saved = categoryRepository.save(Category.builder().name(name).build());
        return ResponseEntity.ok(Map.of("id", (Object) saved.getId(), "name", saved.getName()));
    }

    // Renombra una categoria en todos los productos + en la tabla categories si existe
    @Transactional
    @PutMapping("/categories/rename")
    public ResponseEntity<Map<String, Object>> renameCategory(
            @RequestBody Map<String, String> body) {
        String oldName = body.get("oldName");
        String newName = body.get("newName");
        int updated = productRepository.updateCategoryName(oldName, newName);
        categoryRepository.findByName(oldName).ifPresent(cat -> {
            cat.setName(newName);
            categoryRepository.save(cat);
        });
        return ResponseEntity.ok(Map.of("updated", updated, "message", "Categoria renombrada"));
    }

    // Elimina una categoria — solo si ningun producto la usa
    @Transactional
    @DeleteMapping("/categories/{name}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable String name) {
        long count = productRepository.countByCategory(name);
        if (count > 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La categoria tiene " + count + " productos. Reasignalos antes de eliminarla."));
        }
        // Tambien la borra de la tabla categories si fue creada manualmente
        categoryRepository.findByName(name).ifPresent(categoryRepository::delete);
        return ResponseEntity.ok(Map.of("message", "Categoria eliminada"));
    }

    // ---- DENUNCIAS DE COMENTARIOS ----
    @GetMapping("/reports")
    public ResponseEntity<List<Map<String, Object>>> getReports() {
        return ResponseEntity.ok(commentService.getPendingReports());
    }

    // Elimina el comentario denunciado y marca la denuncia como resuelta
    @DeleteMapping("/reports/{reportId}/comment")
    public ResponseEntity<Map<String, String>> deleteReportedComment(@PathVariable Long reportId) {
        commentService.deleteCommentByReport(reportId);
        return ResponseEntity.ok(Map.of("message", "Comentario eliminado."));
    }

    // Ignora la denuncia sin borrar el comentario
    @PutMapping("/reports/{reportId}/ignore")
    public ResponseEntity<Map<String, String>> ignoreReport(@PathVariable Long reportId) {
        commentService.ignoreReport(reportId);
        return ResponseEntity.ok(Map.of("message", "Denuncia ignorada."));
    }
}
