package com.irenewhub.backend.service;

import com.irenewhub.backend.entity.Product;
import com.irenewhub.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service // marca la clase como componente de logica de negocio, Spring lo detecta y lo registra en su contenedor

@RequiredArgsConstructor // (Lombok), genera el constructor con todos los campos final.
/*Equivale a este constructor:

* public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
}   */

public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        //findById() - orElseThrow devuelve un Optional <Product> porque el producto puede no existir.ç
        // si existe lo devuelve , si no lanza una excepcion
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    @Transactional //Garantiza que si algo falla a mitad de la operacion la bdd vuelve al estado anterior
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);

        product.setName(productDetails.getName());
        product.setCategory(productDetails.getCategory());
        product.setPrice(productDetails.getPrice());
        product.setOriginalPrice(productDetails.getOriginalPrice());
        product.setDescription(productDetails.getDescription());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStock(productDetails.getStock());
        product.setIsOem(productDetails.getIsOem());
        product.setCompatibility(productDetails.getCompatibility());
        product.setWarranty(productDetails.getWarranty());
        product.setCondition(productDetails.getCondition());
        product.setManufacturer(productDetails.getManufacturer());

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }
}