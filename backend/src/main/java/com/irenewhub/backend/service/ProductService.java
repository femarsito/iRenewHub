package com.irenewhub.backend.service;

import com.irenewhub.backend.dto.ProductResponse;
import com.irenewhub.backend.entity.Product;
import com.irenewhub.backend.exception.ResourceNotFoundException;
import com.irenewhub.backend.repository.OrderRepository;
import com.irenewhub.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service //marca esta clase como componente de logica de negocio. Spring le detecta y la registra en su contenedor
@RequiredArgsConstructor // (Lombok), genera el constructor con todos los campos, con su inyeccion de dependencias
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true) //Transaccional garantiza que si algo falla a mitad de la operacion la bdd vuelve al estado anteripr
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategory(category)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Búsqueda por nombre, sin distinguir mayúsculas. Ej: "pantalla" -> "Pantalla OLED iPhone 14"
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(Product product) {
        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, Product productDetails) {
        //Elsethrow - findById(), devuelve un Optional<Product> porqueel producto puede no existir
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id));

        product.setName(productDetails.getName());
        product.setCategory(productDetails.getCategory());
        product.setPrice(productDetails.getPrice());
        product.setOriginalPrice(productDetails.getOriginalPrice());
        product.setDescription(productDetails.getDescription());
        product.setImageUrl(productDetails.getImageUrl());
        product.setStock(productDetails.getStock());
        product.setIsOem(productDetails.getIsOem());
        product.setWarranty(productDetails.getWarranty());
        product.setCondition(productDetails.getCondition());
        product.setManufacturer(productDetails.getManufacturer());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + id));

        // Comprobar si algun pedido contiene este producto
        // Si es asi, no se puede borrar porque romperia el historial de pedidos
        long pedidosAsociados = orderRepository.countOrdersByProductId(id);
        if (pedidosAsociados > 0) {
            throw new RuntimeException(
                "No se puede eliminar: este producto aparece en " + pedidosAsociados + " pedido(s).");
        }

        productRepository.delete(product);
    }

    // Convierte entidad -> DTO (dentro de la transacción, sesión abierta)
    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .originalPrice(product.getOriginalPrice())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .stock(product.getStock())
                .isOem(product.getIsOem())
                .warranty(product.getWarranty())
                .condition(product.getCondition())
                .manufacturer(product.getManufacturer())
                .build();
    }
}