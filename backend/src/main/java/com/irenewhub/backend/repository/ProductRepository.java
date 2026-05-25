package com.irenewhub.backend.repository;

// ---- REPOSITORIO DE PRODUCTOS ----
// Interfaz que gestiona todas las consultas a la tabla products.
// JpaRepository<Product, Long> ya incluye de serie:
//   findAll(), findById(), save(), deleteById(), count(), existsById()...
//
// Los metodos con nombre derivado (findBy...) los genera Spring leyendo el nombre:
//   findByCategory              → WHERE category = ?
//   findByNameContainingIgnoreCase → WHERE LOWER(name) LIKE LOWER('%?%')
//
// Los metodos con @Query usan JPQL: igual que SQL pero con nombres de clases Java
// en vez de nombres de tablas (Product en vez de products, p.category en vez de category)

import com.irenewhub.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository // registra esta interfaz como componente de Spring para inyectarla con @Autowired
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Spring genera: SELECT * FROM products WHERE category = ?
    // Se usa cuando el usuario filtra por categoria en el catalogo
    List<Product> findByCategory(String category);

    // Spring genera: SELECT * FROM products WHERE is_oem = ?
    // Se usa para filtrar productos originales (OEM = Original Equipment Manufacturer)
    List<Product> findByIsOem(Boolean isOem);

    // Spring genera: SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%?%')
    // IgnoreCase → no distingue mayusculas/minusculas: "pantalla" encuentra "Pantalla"
    // Containing → busca el texto en cualquier posicion del nombre (LIKE %texto%)
    List<Product> findByNameContainingIgnoreCase(String name);

    // @Query con JPQL: no se puede generar por nombre porque usa DISTINCT y ORDER BY
    // Devuelve los nombres de categoria unicos ordenados alfabeticamente
    // Se usa en el panel admin para listar las categorias disponibles
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findAllDistinctCategories();

    // Spring genera: SELECT COUNT(*) FROM products WHERE category = ?
    // Se usa antes de eliminar una categoria: si tiene productos no se puede borrar
    long countByCategory(String category);

    // @Modifying indica que esta query modifica datos (UPDATE), no solo los lee
    // @Param conecta el parametro Java con el :nombre dentro de la query
    // Renombra la categoria en TODOS los productos que la usan de una sola vez
    @Modifying
    @Query("UPDATE Product p SET p.category = :newName WHERE p.category = :oldName")
    int updateCategoryName(@Param("oldName") String oldName, @Param("newName") String newName); // Panel ADMIN

    // @Modifying necesario porque es un UPDATE directo a la BD
    // Suma o resta stock sin pasar por el cache de Hibernate (mas fiable para operaciones concurrentes)
    // delta positivo → suma stock (cuando se cancela un pedido, se devuelve el stock)
    // delta negativo → resta stock (cuando se reactiva un pedido cancelado)
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock + :delta WHERE p.id = :id") // Panel ADMIN
    void updateStock(@Param("id") Long id, @Param("delta") int delta);
}
