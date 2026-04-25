package com.irenewhub.backend.repository;

// ─── REPOSITORIO DE PRODUCTOS ─────────────────────────────────────────────────
// Spring Data JPA genera el SQL automáticamente.
// JpaRepository<Product, Long> ya incluye findAll(), findById(), save(), delete(), count()...
//
// Los métodos con nombre derivado (findBy...) los genera Spring leyendo el nombre:
//   findByCategory         → WHERE category = ?
//   findByNameContaining   → WHERE name LIKE %?%
//
// El método findByCompatibility usa @Query porque la compatibilidad está en una tabla
// separada (product_compatibility) y el JOIN no lo puede deducir Spring del nombre solo.

import com.irenewhub.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Filtrar por categoría exacta → SELECT * FROM products WHERE category = ?
    List<Product> findByCategory(String category);

    // Filtrar por tipo OEM → SELECT * FROM products WHERE is_oem = ?
    List<Product> findByIsOem(Boolean isOem);

    // Buscar por nombre (ignora mayúsculas/minúsculas)
    // → SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%?%')
    List<Product> findByNameContainingIgnoreCase(String name);

    // Filtrar por modelo de iPhone compatible.
    // Necesita @Query porque la compatibilidad está en la tabla "product_compatibility"
    // y hay que hacer un JOIN explícito con la colección de strings.
    // JPQL (Java Persistence Query Language) usa nombres de clases y atributos Java,
    // no nombres de tablas SQL → "p.compatibility" hace referencia al List<String> de la entidad.
    @Query("SELECT p FROM Product p JOIN p.compatibility c WHERE LOWER(c) LIKE LOWER(CONCAT('%', :model, '%'))")
    List<Product> findByCompatibilityContaining(@Param("model") String model);

    // Devuelve todos los nombres de categoría únicos ordenados alfabéticamente
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllDistinctCategories();

    // Cuántos productos pertenecen a una categoría (para saber si se puede eliminar)
    long countByCategory(String category);

    // Cambia el nombre de categoría en todos los productos que la usan
    @Modifying
    @Query("UPDATE Product p SET p.category = :newName WHERE p.category = :oldName")
    int updateCategoryName(@Param("oldName") String oldName, @Param("newName") String newName);
}
