package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE REPORTES DE COMENTARIOS ----
// Interfaz que gestiona las consultas a la tabla comment_reports.
// Un reporte es una denuncia de un usuario sobre un comentario inapropiado.
// El admin puede revisar los reportes pendientes desde el panel de administracion.
// ReportStatus es un enum dentro de CommentReport: PENDING (pendiente) o REVIEWED (revisado).

import com.irenewhub.backend.entity.CommentReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // registra esta interfaz como componente de Spring para inyectarla con @Autowired
public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {

    // Spring genera: SELECT * FROM comment_reports WHERE status = ? ORDER BY created_at DESC
    // CommentReport.ReportStatus es el enum: se pasa PENDING para ver los sin revisar
    List<CommentReport> findByStatusOrderByCreatedAtDesc(CommentReport.ReportStatus status);  // listar los reportes pendientes de revision


    // Spring genera: SELECT COUNT(*) > 0 FROM comment_reports WHERE comment_id = ? AND reporter_email = ?
    boolean existsByCommentIdAndReporterEmail(Long commentId, String reporterEmail);// Se usa antes de guardar un reporte nuevo para evitar que el mismo usuario reporte dos veces el mismo comentario

}
