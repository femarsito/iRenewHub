package com.irenewhub.backend.config;

import com.irenewhub.backend.entity.Product;
import com.irenewhub.backend.entity.User;
import com.irenewhub.backend.repository.ProductRepository;
import com.irenewhub.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor

// CommandLineRunner, es una interfaz de Spring Boot que ejectutra el metodo IF de abajo
public class DataSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // Crear admin si no existe
        if (!userRepository.existsByEmail("admin@irenewhub.com")) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("iRenewHub")
                    .email("admin@irenewhub.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println("Usuario admin creado: admin@irenewhub.com");
        }

        //Este IF es la protección más importante. Como ahora usamos `create` en lugar de `create-drop`, las tablas persisten entre reinicios. Sin esta comprobación, cada vez que reiniciaras la aplicación insertaría 6 productos más, duplicándolos indefinidamente.
        if (productRepository.count() > 0) {
            System.out.println("Base de datos ya tiene productos. Seeder omitido.");
            return;
        }

        System.out.println("Insertando productos iniciales...");

        List<Product> products = List.of(
// Este Product.builder() es el patrón Builder de Lombok (`@Builder` en la entidad). En lugar de un constructor con 13 argumentos donde es fácil equivocarse de orden, construyes el objeto campo a campo de forma legible.
                Product.builder()
                        .name("Pantalla OLED iPhone 14 Pro")
                        .category("Pantallas")
                        .price(new BigDecimal("299.99"))
                        .originalPrice(new BigDecimal("399.99"))
                        .description("Pantalla OLED original certificada por Apple. Calidad premium con tecnología ProMotion 120Hz.")
                        .imageUrl("assets/products/pantalla-iphone-14-pro.jpg")
                        .stock(15)
                        .isOem(true)
                        .compatibility(List.of("iPhone 14 Pro"))
                        .warranty("12 meses")
                        .condition("Nuevo")
                        .manufacturer("Apple Certified")
                        .build(),

                Product.builder()
                        .name("Batería iPhone 13")
                        .category("Baterías")
                        .price(new BigDecimal("49.99"))
                        .originalPrice(null)
                        .description("Batería de reemplazo con certificación Apple. Capacidad de 3227mAh.")
                        .imageUrl("assets/products/bateria-iphone-13.jpg")
                        .stock(30)
                        .isOem(true)
                        .compatibility(List.of("iPhone 13", "iPhone 13 Pro"))
                        .warranty("6 meses")
                        .condition("Nuevo")
                        .manufacturer("Apple Certified")
                        .build(),

                Product.builder()
                        .name("Cámara Trasera iPhone 15 Pro Max")
                        .category("Cámaras")
                        .price(new BigDecimal("179.99"))
                        .originalPrice(new BigDecimal("249.99"))
                        .description("Sistema de cámara triple de 48MP. Incluye sensor principal, ultra gran angular y telefoto.")
                        .imageUrl("assets/products/camara-iphone-15-pro-max.jpg")
                        .stock(8)
                        .isOem(true)
                        .compatibility(List.of("iPhone 15 Pro Max"))
                        .warranty("12 meses")
                        .condition("Nuevo")
                        .manufacturer("Apple OEM")
                        .build(),

                Product.builder()
                        .name("Pantalla LCD iPhone 11 (Genérica)")
                        .category("Pantallas")
                        .price(new BigDecimal("89.99"))
                        .originalPrice(null)
                        .description("Pantalla LCD de alta calidad genérica. Certificada por Apple, excelente relación calidad-precio.")
                        .imageUrl("assets/products/pantalla-iphone-11.jpg")
                        .stock(25)
                        .isOem(false)
                        .compatibility(List.of("iPhone 11"))
                        .warranty("6 meses")
                        .condition("Nuevo")
                        .manufacturer("Apple Certified Generic")
                        .build(),

                Product.builder()
                        .name("Conector de Carga Lightning iPhone 12")
                        .category("Conectores")
                        .price(new BigDecimal("39.99"))
                        .originalPrice(null)
                        .description("Puerto de carga Lightning original. Incluye micrófono y cables flex.")
                        .imageUrl("assets/products/conector-iphone-12.jpg")
                        .stock(40)
                        .isOem(true)
                        .compatibility(List.of("iPhone 12", "iPhone 12 Mini", "iPhone 12 Pro"))
                        .warranty("6 meses")
                        .condition("Nuevo")
                        .manufacturer("Apple Certified")
                        .build(),

                Product.builder()
                        .name("Altavoz Auricular iPhone 13 Pro")
                        .category("Audio")
                        .price(new BigDecimal("29.99"))
                        .originalPrice(null)
                        .description("Altavoz superior para llamadas. Audio cristalino y compatible con Spatial Audio.")
                        .imageUrl("assets/products/altavoz-iphone-13-pro.jpg")
                        .stock(20)
                        .isOem(true)
                        .compatibility(List.of("iPhone 13 Pro", "iPhone 13 Pro Max"))
                        .warranty("3 meses")
                        .condition("Nuevo")
                        .manufacturer("Apple OEM")
                        .build()
        );
// El saveAll() inserta todos los productos en una sola operación de base de datos, mucho más eficiente que llamar a `save()` seis veces en un bucle.
        productRepository.saveAll(products);
        System.out.println(products.size() + " productos insertados correctamente.");
    }
}