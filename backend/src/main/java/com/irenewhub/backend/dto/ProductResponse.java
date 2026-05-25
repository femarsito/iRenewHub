package com.irenewhub.backend.dto;

// ---- DTO: PRODUCT RESPONSE ----
// JSON que el backend devuelve al frontend cuando pide productos.
// Copia los mismos campos de la entidad Product pero como DTO independiente.
// Asi si en el futuro Product tiene campos internos, no se exponen al exterior.

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data           // genera getters, setters, equals(), hashCode() y toString()
@Builder        // ProductResponse.builder().name("Bateria").price(49.99).build()
@NoArgsConstructor // constructor vacio requerido por Jackson para serializar a JSON
@AllArgsConstructor // constructor con todos los campos, necesario para que @Builder funcione
public class ProductResponse {

    private Long id;                // identificador unico del producto en la BD
    private String name;            // nombre del producto
    private String category;        // categoria: "Baterias", "Pantallas"...
    private BigDecimal price;       // precio actual de venta
    private BigDecimal originalPrice; // precio antes del descuento (puede ser null si no hay descuento)
    private String description;     // descripcion del producto
    private String imageUrl;        // ruta de la imagen
    private Integer stock;          // unidades disponibles (0 = agotado)
    private Boolean isOem;          // true = pieza original OEM, false = compatible
    private String warranty;        // garantia: "Sin garantia", "3 meses", "6 meses", "12 meses"
    private String condition;       // "Nuevo" o "Reacondicionado"
    private String manufacturer;    // fabricante del repuesto
}
