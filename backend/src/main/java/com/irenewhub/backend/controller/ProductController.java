package com.irenewhub.backend.controller;

import com.irenewhub.backend.entity.Product;
import com.irenewhub.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //combina @Controller + @ResponseBody. Todos devuelven datos JSON y no HTML.
@RequestMapping("/api/products") //todas las rutas de este controlador empiezan por eso
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /*@GetMapping, @PostMapping, @PutMapping, @DeleteMapping:
    mapean métodos HTTP a métodos Java. Esto es exactamente lo que tu Angular llama.
    */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts(
            //@RequestParam — captura parámetros de query. GET /api/products?category=Baterías → category = "Baterías".
            @RequestParam(required = false) String category) {

        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(productService.getProductsByCategory(category));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    //@PathVariable captura el {id} de la URL. GET /api/products/3 → id = 3.
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    // @RequestBody — convierte el JSON del cuerpo de la petición en un objeto Java automáticamente.
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        Product created = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            //ResponseEntity — te permite controlar el código HTTP de respuesta. 200 OK, 201 CREATED, 204 No Content, etc.
            @RequestBody Product product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}