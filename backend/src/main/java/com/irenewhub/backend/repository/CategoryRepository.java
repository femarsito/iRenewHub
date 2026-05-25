package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE CATEGORIAS ----
// Interfaz que gestiona las consultas a la tabla categories.
// Las categorias agrupan los productos (Pantallas, Baterias, Camaras...).
// El nombre de categoria es unico: no pueden existir dos con el mismo nombre.

import com.irenewhub.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // registra esta interfaz como componente de Spring para inyectarla con @Autowired
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Spring genera: SELECT COUNT(*) > 0 FROM categories WHERE name = ?
    // Se usa al crear una nueva categoria para evitar nombres duplicados
    boolean existsByName(String name);

    // Spring genera: SELECT * FROM categories WHERE name = ?
    // Devuelve Optional porque la categoria puede no existir
    // Se usa al renombrar o eliminar una categoria desde el panel admin
    Optional<Category> findByName(String name);
}
