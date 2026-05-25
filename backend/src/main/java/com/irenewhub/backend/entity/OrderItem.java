package com.irenewhub.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// ---- ENTIDAD ORDER ITEM ----
// Representa la tabla "order_items" en PostgreSQL.
// Es la linea de detalle de un pedido: que producto, cuantas unidades y a que precio.
// Un pedido con 3 productos distintos tiene 3 filas en esta tabla.
//
// El precio (unitPrice) se copia en el momento de la compra para que si el precio
// del producto cambia despues, el historial de compras no se vea afectado.

@Entity
@Table(name = "order_items")
@Data // genera getters, setters, equals(), hashCode() y toString() automaticamente
@Builder
@NoArgsConstructor // constructor vacio, necesario para JPA
@AllArgsConstructor // constructor con todos los campos, necesario para @Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // autoincrement
    private Long id;

    // @ManyToOne → Muchas líneas de carrito pueden pertenecer al mismo pedido (ticket)
    // FetchType.LAZY → No carga los datos del pedido (quién compra, fecha, etc.) hasta que no los pidas expresamente (ahorra memoria)
    // @JoinColumn → Crea la columna "order_id" en la tabla para saber a qué pedido pertenece este item
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // @ManyToOne → Muchas personas/pedidos pueden comprar el mismo producto del catálogo
    // FetchType.EAGER → Carga el producto (nombre, precio, foto) inmediatamente, porque sin el producto el item no tiene sentido
    // @JoinColumn → Crea la columna "product_id" para saber qué repuesto específico es el que se ha comprado
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity; // unidades compradas de este producto

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice; // precio unitario EN EL MOMENTO DE LA COMPRA (copia del producto)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal; // unitPrice * quantity
}
