package com.irenewhub.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    // Items del carrito
    @NotEmpty(message = "El pedido debe tener al menos un producto")
    private List<OrderItemRequest> items;

    // Datos de envío — exactamente los mismos campos del checkout Angular
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank private String email;
    @NotBlank @Pattern(regexp = "^[0-9]{9}$") private String phone;
    @NotBlank private String address;
    @NotBlank private String city;
    @NotBlank @Pattern(regexp = "^[0-9]{5}$") private String postalCode;
    @NotBlank private String province;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}