package com.irenewhub.backend.dto;

// ---- DTO: ORDER RESPONSE ----
// JSON que el backend devuelve cuando el frontend pide un pedido o crea uno nuevo.
// Contiene los datos del pedido y, dentro, la lista de productos comprados (items).
// OrderItemResponse es una clase interna estatica: vive dentro de este DTO porque
// solo tiene sentido en el contexto de un pedido y no se usa en ningun otro sitio.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data           // genera getters, setters, equals(), hashCode() y toString()
@Builder        // OrderResponse.builder().orderNumber("IPH-...").total(99.99).build()
@NoArgsConstructor // constructor vacio requerido por Jackson
@AllArgsConstructor // constructor con todos los campos, necesario para @Builder
public class OrderResponse {

    private Long id;                    // id del pedido en la BD
    private String orderNumber;         // codigo visible del pedido: "IPH-78B35F83"
    private String customerName;        // nombre completo del cliente (firstName + lastName)
    private String customerEmail;       // email del cliente para identificarle
    private String status;              // estado actual: "PENDING", "CONFIRMED", "SHIPPED"...
    private BigDecimal subtotal;        // suma de todos los items sin envio
    private BigDecimal shippingCost;    // gastos de envio (0 si supera el minimo gratuito)
    private BigDecimal total;           // total final = subtotal + envio - descuento
    private LocalDateTime createdAt;    // fecha y hora de creacion del pedido
    private List<OrderItemResponse> items; // lista de productos comprados en este pedido

    // Clase interna estatica: representa cada linea del pedido (un producto + cantidad + precio)
    // Es estatica porque no necesita acceder a los campos de OrderResponse para existir
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long productId;         // id del producto comprado
        private String productName;     // nombre del producto en el momento de la compra
        private Integer quantity;       // unidades compradas
        private BigDecimal unitPrice;   // precio unitario en el momento de la compra (copia fija)
        private BigDecimal subtotal;    // unitPrice * quantity
    }
}
