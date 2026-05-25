package com.irenewhub.backend.controller;

import com.irenewhub.backend.repository.CouponRepository;
import com.irenewhub.backend.repository.UserRepository;
import com.irenewhub.backend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;

    // ---- OBTENER COMENTARIOS ----

    @GetMapping("/api/products/{productId}/comments")
    public ResponseEntity<List<Map<String, Object>>> getComments(
            @PathVariable Long productId,
            Authentication auth) {
        String email = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(commentService.getComments(productId, email));
    }

    // ---- AÑADIR COMENTARIO (requiere compra para comentarios raíz) ----

    @PostMapping("/api/products/{productId}/comments")
    public ResponseEntity<Map<String, Object>> addComment(
            @PathVariable Long productId,
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        String email = auth.getName();
        String userName = resolveUserName(email);
        String content  = (String) body.get("content");
        Long   parentId = body.get("parentId") != null
                ? Long.valueOf(body.get("parentId").toString()) : null;

        Map<String, Object> result = commentService.addComment(productId, email, userName, content, parentId);
        return ResponseEntity.ok(result);
    }

    // ---- LIKE / UNLIKE ----

    @PostMapping("/api/comments/{commentId}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long commentId,
            Authentication auth) {
        return ResponseEntity.ok(commentService.toggleLike(commentId, auth.getName()));
    }

    // ---- DENUNCIAR COMENTARIO ----

    @PostMapping("/api/comments/{commentId}/report")
    public ResponseEntity<Map<String, String>> reportComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String email = auth.getName();
        String name  = resolveUserName(email);
        commentService.reportComment(commentId, email, name, body.get("reason"));
        return ResponseEntity.ok(Map.of("message", "Denuncia enviada. El administrador la revisará."));
    }

    // ---- VERIFICAR COMPRA ----

    @GetMapping("/api/products/{productId}/can-comment")
    public ResponseEntity<Map<String, Boolean>> canComment(
            @PathVariable Long productId,
            Authentication auth) {
        boolean can = commentService.hasPurchased(auth.getName(), productId);
        return ResponseEntity.ok(Map.of("canComment", can));
    }

    // ---- CUPONES DEL USUARIO ----

    @GetMapping("/api/coupons/my")
    public ResponseEntity<List<Map<String, Object>>> getMyCoupons(Authentication auth) {
        return ResponseEntity.ok(commentService.getCoupons(auth.getName()));
    }

    // ---- VALIDAR CUPÓN ----

    @GetMapping("/api/coupons/validate")
    public ResponseEntity<Map<String, Object>> validateCoupon(
            @RequestParam String code,
            Authentication auth) {
        Map<String, Object> response = new LinkedHashMap<>();
        couponRepository.findByCodeAndUserEmailAndUsedFalse(code.trim().toUpperCase(), auth.getName())
                .ifPresentOrElse(
                        c -> {
                            response.put("valid", true);
                            response.put("discountPercent", c.getDiscountPercent());
                        },
                        () -> {
                            response.put("valid", false);
                            response.put("message", "Cupón no válido, ya utilizado o no te pertenece.");
                        });
        return ResponseEntity.ok(response);
    }

    // Obtiene el nombre completo del usuario desde la BD
    private String resolveUserName(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getFirstName() + " " + u.getLastName())
                .orElse("Usuario");
    }
}
