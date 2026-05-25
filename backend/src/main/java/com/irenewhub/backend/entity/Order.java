package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// ---- ENTIDAD ORDER ----
// Representa la tabla "orders" en PostgreSQL.
// Cada vez que un usuario completa una compra se crea una fila en esta tabla.
// Guarda: datos del comprador, direccion de envio, importes y estado del pedido.
// Los productos comprados se guardan en la tabla order_items (relacion 1 a N).

@Entity
@Table(name = "orders")
@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
@Builder // permite crear pedidos con Order.builder().orderNumber("...").build()
@NoArgsConstructor // constructor vacio, necesario para JPA
@AllArgsConstructor // constructor con todos los campos, necesario para @Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false) // codigo del pedido, ej: "IPH-78B35F83"
    private String orderNumber;

    // @ManyToOne → muchos pedidos pueden pertenecer a un mismo usuario
    // FetchType.LAZY → no carga los datos del usuario automaticamente, solo cuando se accede al campo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // crea la columna "user_id" en orders como FK apuntando a users.id
    private User user;

    // @OneToMany → un pedido tiene muchas lineas de producto
    // mappedBy = "order" → la relacion la gestiona el campo "order" dentro de OrderItem
    // CascadeType.ALL → si guardas o borras un Order, sus OrderItems se guardan o borran solos
    // orphanRemoval → si quitas un item de la lista, se borra de la BD automaticamente
    // FetchType.EAGER → carga los items siempre al cargar el pedido (necesario para restaurar stock al cancelar)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal; // suma de todos los items antes del envio

    @Column(name = "shipping_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingCost; // gastos de envio (0.00 si supera el minimo gratuito)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total; // total final = subtotal + envio - descuento de cupon

    @Enumerated(EnumType.STRING) // guarda el estado como texto: "PENDING", "CONFIRMED"...
    @Column(nullable = false)
    private OrderStatus status;

    // Datos de envio copiados del formulario de checkout.
    // Se guardan directamente aqui (no como FK al usuario) para que si el usuario
    // cambia su perfil despues, el historial del pedido no se vea afectado.
    @Column(name = "shipping_first_name", nullable = false)
    private String shippingFirstName;

    @Column(name = "shipping_last_name", nullable = false)
    private String shippingLastName;

    @Column(name = "shipping_email", nullable = false)
    private String shippingEmail;

    @Column(name = "shipping_phone", nullable = false)
    private String shippingPhone;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Column(name = "shipping_city", nullable = false)
    private String shippingCity;

    @Column(name = "shipping_postal_code", nullable = false)
    private String shippingPostalCode;

    @Column(name = "shipping_province", nullable = false)
    private String shippingProvince;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @PrePersist // este metodo se ejecuta automaticamente ANTES del primer INSERT. JPA lo llama solo, no hay que llamarlo manualmente
    public void prePersist() {
        this.createdAt = LocalDateTime.now(); // fecha y hora de creacion del pedido
        if (this.status == null) {
            this.status = OrderStatus.PENDING; // estado inicial siempre es PENDING
        }
    }

    public enum OrderStatus { // estados posibles del pedido
        PENDING,   // recien creado, pendiente de confirmacion
        CONFIRMED, // confirmado por el administrador
        SHIPPED,   // enviado al cliente
        DELIVERED, // entregado al cliente
        CANCELLED  // cancelado
    }
}
