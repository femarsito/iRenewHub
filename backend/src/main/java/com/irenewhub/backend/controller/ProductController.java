package com.irenewhub.backend.controller;

import com.irenewhub.backend.dto.ProductResponse;
import com.irenewhub.backend.entity.Product;
import com.irenewhub.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController //Combina @Controller + @ResponseBody, significa que todos los metodos devuelven datos JSON, no HTML
@RequestMapping("/api/products") //todas las rutas de este controlador empiezan por  /api/products
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /*@GetMapping, @PostMapping, @PutMapping, @DeleteMapping
    * mapean métodos HTTP a métodos Java. Esto es exactamente lo que tu Angular llama.
    */

    @GetMapping
    //ResponseEntity, permite controlar el codigo HTTP de respuesta  (200 OK, 201 CREATED, 204 NO CONTENT...)
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(required = false) String category,  // ?category=Baterias
            @RequestParam(required = false) String search) {  // ?search=pantalla

        // Los parametros son opcionales: se comprueba en orden de prioridad
        if (search != null && !search.isEmpty()) {
            return ResponseEntity.ok(productService.searchProducts(search));
        }
        if (category != null && !category.isEmpty()) {
            return ResponseEntity.ok(productService.getProductsByCategory(category));
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) { //PathVariable captura el id de la URL. GET /api/products/3 -> id=3
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody Product product) { //RequestBody convierte el JSON del cuerpo de la petición en un objeto Java
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id, //Captura parametros de query. GET /api/products/
            @RequestBody Product product) { //convierte el JSON del cuerpo de la peticion en una Objeto de JAVA
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
