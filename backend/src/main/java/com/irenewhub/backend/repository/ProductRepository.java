package com.irenewhub.backend.repository;

import com.irenewhub.backend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

/* JpaRepository<Product,Long ya me da todo esto sin yo tener que escribir nada:
*
* - finfAll() -> SELECT * FROM products
* - findById(1L) -> SELECT * FROM products WHERE id = 1
* - save(product) -> INSERT o UPDATE automaticamente
* - deleteById(1L) - DELETE FROM product WHERE id = 1
* - count() -> SELECT COUNT(*) FROM products
* */
public interface ProductRepository extends JpaRepository<Product, Long> {
    //Estos dos metodos los generea Spring automaticamente leyendo el nombre del metodo
    List<Product> findByCategory(String category); // SELECT * FROM products WHERE category = ?

    List<Product> findByIsOem(Boolean isOem);
}