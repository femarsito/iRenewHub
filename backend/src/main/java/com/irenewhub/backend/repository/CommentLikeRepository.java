package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE LIKES DE COMENTARIOS ----
// Interfaz que gestiona las consultas a la tabla comment_likes.
// Cada fila representa que un usuario ha dado like a un comentario concreto.
// La combinacion (commentId + userEmail) es unica: un usuario no puede dar dos likes al mismo comentario.

import com.irenewhub.backend.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository // registra esta interfaz como componente de Spring para inyectarla con @Autowired
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // Spring genera: SELECT COUNT(*) > 0 FROM comment_likes WHERE comment_id = ? AND user_email = ?
    boolean existsByCommentIdAndUserEmail(Long commentId, String userEmail);// Se usa antes de dar like para evitar duplicados: si ya existe, se quita el like en vez de añadir


    // Spring genera: DELETE FROM comment_likes WHERE comment_id = ? AND user_email = ?
    void deleteByCommentIdAndUserEmail(Long commentId, String userEmail);  // Se usa al hacer "toggle" del like: si ya tenia like se elimina la fila


    // Spring genera: SELECT COUNT(*) FROM comment_likes WHERE comment_id = ?
    // Se usa para mostrar el numero de likes de cada comentario
    long countByCommentId(Long commentId);

    // No se puede generar por nombre porque devuelve solo los IDs (no objetos completos)
    // Devuelve un Set con los IDs de comentarios que ha likeado el usuario
    // Set en vez de List porque no importa el orden y no pueden repetirse IDs
    // Se usa para que el frontend sepa que botones de like debe mostrar como activos
    @Query("SELECT cl.commentId FROM CommentLike cl WHERE cl.userEmail = :userEmail")
    Set<Long> findLikedCommentIdsByUserEmail(@Param("userEmail") String userEmail);
}
