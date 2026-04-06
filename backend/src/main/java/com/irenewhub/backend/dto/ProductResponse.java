package com.irenewhub.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// Lombok se encarga practicamente de la clase ompleta con los @
@Data //getters, setters, toString(), equals(), hashCode()
@Builder //me deja crear instancias de forma fluida como ProductResponse.builder().name("Teclado").price(10.0).build();.
@NoArgsConstructor //constructor vacio
@AllArgsConstructor //constructor con todos los capos
public class ProductResponse {

    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String description;
    private String imageUrl;
    private Integer stock;
    private Boolean isOem;
    private List<String> compatibility;
    private String warranty;
    private String condition;
    private String manufacturer;
}