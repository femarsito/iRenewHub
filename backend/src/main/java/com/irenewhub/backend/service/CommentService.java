package com.irenewhub.backend.service;

import com.irenewhub.backend.entity.*;
import com.irenewhub.backend.exception.ResourceNotFoundException;
import com.irenewhub.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentReportRepository commentReportRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    // ---- CONSULTAR COMENTARIOS ----

    public List<Map<String, Object>> getComments(Long productId, String currentUserEmail) {
        Set<Long> likedByUser = currentUserEmail != null
                ? commentLikeRepository.findLikedCommentIdsByUserEmail(currentUserEmail)
                : Collections.emptySet();

        List<Comment> roots = commentRepository
                .findByProductIdAndParentIdIsNullOrderByCreatedAtDesc(productId);

        return roots.stream()
                .map(c -> buildCommentMap(c, likedByUser, currentUserEmail))
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildCommentMap(Comment c, Set<Long> likedByUser, String currentUserEmail) {
        List<Map<String, Object>> replies = commentRepository
                .findByParentIdOrderByCreatedAtAsc(c.getId())
                .stream()
                .map(r -> buildCommentMap(r, likedByUser, currentUserEmail))
                .collect(Collectors.toList());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id",          c.getId());
        map.put("userName",    c.getUserName());
        map.put("userEmail",   c.getUserEmail());
        map.put("content",     c.getContent());
        map.put("parentId",    c.getParentId());
        map.put("likes",       c.getLikes());
        map.put("hasLiked",    likedByUser.contains(c.getId()));
        map.put("isOwn",       c.getUserEmail().equals(currentUserEmail));
        map.put("createdAt",   c.getCreatedAt().toString());
        map.put("replies",     replies);
        return map;
    }

    // ---- AÑADIR COMENTARIO ----

    @Transactional
    public Map<String, Object> addComment(Long productId, String userEmail, String userName,
                                          String content, Long parentId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Producto no encontrado: " + productId);
        }

        // Solo comentarios raíz requieren haber comprado el producto
        if (parentId == null && orderRepository.countPurchaseByUserAndProduct(userEmail, productId) == 0) {
            throw new IllegalStateException("Solo puedes comentar productos que hayas comprado.");
        }

        Comment comment = Comment.builder()
                .productId(productId)
                .userEmail(userEmail)
                .userName(userName)
                .content(content)
                .parentId(parentId)
                .build();

        commentRepository.save(comment);
        return buildCommentMap(comment, Collections.emptySet(), userEmail);
    }

    // ---- LIKE / UNLIKE ----

    @Transactional
    public Map<String, Object> toggleLike(Long commentId, String userEmail) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado: " + commentId));

        boolean alreadyLiked = commentLikeRepository.existsByCommentIdAndUserEmail(commentId, userEmail);

        if (alreadyLiked) { //QUITA EL LIKE QUE YA HABÍA
            commentLikeRepository.deleteByCommentIdAndUserEmail(commentId, userEmail);
        } else { // PONE EL LIKE PORQUE NO HABÍA
            commentLikeRepository.save(CommentLike.builder()
                    .commentId(commentId).userEmail(userEmail).build());
        }

        // Contar likes reales desde la base de datos
        long totalLikes = commentLikeRepository.countByCommentId(commentId);
        comment.setLikes((int) totalLikes);

        // Si llega a 5 likes por primera vez, el autor recibe un cupón del 5%
        if (totalLikes >= 5 && !comment.isCouponGranted()) {
            String code = "IRH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            couponRepository.save(Coupon.builder()
                    .userEmail(comment.getUserEmail())
                    .code(code)
                    .discountPercent(5)
                    .build());
            comment.setCouponGranted(true);
        }

        commentRepository.save(comment);  // ACTUALIZA FILA DEL COMENTARIO DE LA TABLA COMMENTS
        return Map.of("likes", comment.getLikes(), "hasLiked", !alreadyLiked);
    }

    // ---- DENUNCIAR ----

    @Transactional
    public void reportComment(Long commentId, String reporterEmail, String reporterName, String reason) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado: " + commentId));

        if (commentReportRepository.existsByCommentIdAndReporterEmail(commentId, reporterEmail)) {
            throw new IllegalStateException("Ya has denunciado este comentario.");
        }

        commentReportRepository.save(CommentReport.builder()
                .commentId(commentId)
                .commentContent(comment.getContent())
                .reporterEmail(reporterEmail)
                .reporterName(reporterName)
                .reportedUserEmail(comment.getUserEmail())
                .reportedUserName(comment.getUserName())
                .reason(reason)
                .build());
    }

    // ---- CUPONES DEL USUARIO ----

    public List<Map<String, Object>> getCoupons(String userEmail) {
        return couponRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(c -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id",              c.getId());
                    map.put("code",            c.getCode());
                    map.put("discountPercent", c.getDiscountPercent());
                    map.put("used",            c.isUsed());
                    map.put("createdAt",       c.getCreatedAt().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ---- ADMIN: DENUNCIAS ----

    public List<Map<String, Object>> getPendingReports() {
        return commentReportRepository
                .findByStatusOrderByCreatedAtDesc(CommentReport.ReportStatus.PENDING)
                .stream()
                .map(r -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id",                r.getId());
                    map.put("commentId",         r.getCommentId());
                    map.put("commentContent",    r.getCommentContent());
                    map.put("reporterEmail",     r.getReporterEmail());
                    map.put("reporterName",      r.getReporterName());
                    map.put("reportedUserEmail", r.getReportedUserEmail());
                    map.put("reportedUserName",  r.getReportedUserName());
                    map.put("reason",            r.getReason());
                    map.put("createdAt",         r.getCreatedAt().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteCommentByReport(Long reportId) {
        CommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada: " + reportId));
        commentRepository.deleteById(report.getCommentId());
        report.setStatus(CommentReport.ReportStatus.RESOLVED);
        commentReportRepository.save(report);
    }

    @Transactional
    public void ignoreReport(Long reportId) {
        CommentReport report = commentReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Denuncia no encontrada: " + reportId));
        report.setStatus(CommentReport.ReportStatus.IGNORED);
        commentReportRepository.save(report);
    }

    // ---- VERIFICAR COMPRA ----

    public boolean hasPurchased(String userEmail, Long productId) {
        return orderRepository.countPurchaseByUserAndProduct(userEmail, productId) > 0;
    }
}
