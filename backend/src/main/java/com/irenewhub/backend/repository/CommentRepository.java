package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE COMENTARIOS ----
// Interfaz que gestiona las consultas a la tabla comments.
// Los comentarios tienen dos niveles:
//   parentId = null -> comentario raiz: el usuario comenta directamente en el producto
//   parentId = X   -> respuesta: el usuario responde a otro comentario (id X)

import com.irenewhub.backend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // registra esta interfaz como componente de Spring para inyectarla con @Autowired
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Spring lee: findBy ProductId And ParentId IsNull OrderBy CreatedAt Desc
    // Genera: SELECT * FROM comments WHERE product_id = ? AND parent_id IS NULL ORDER BY created_at DESC
    // IsNull -> Spring entiende que parent_id debe ser null (son comentarios raiz, no respuestas)
    List<Comment> findByProductIdAndParentIdIsNullOrderByCreatedAtDesc(Long productId); // Se usa al cargar la seccion de resenas de un producto


    // Spring lee: findBy ParentId OrderBy CreatedAt Asc
    // Genera: SELECT * FROM comments WHERE parent_id = ? ORDER BY created_at ASC
    // Asc -> las respuestas se muestran del mas antiguo al mas reciente (orden cronologico)
    List<Comment> findByParentIdOrderByCreatedAtAsc(Long parentId);  // Se usa para cargar las respuestas anidadas de un comentario concreto

}
