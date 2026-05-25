package com.irenewhub.backend.dto;

// ---- DTO: CREATE ORDER REQUEST ----
// JSON que llega del frontend cuando el usuario confirma una compra en el checkout.
// Contiene dos partes: los productos del carrito (items) y los datos de envio del formulario.
// OrderItemRequest es una clase interna estatica que representa cada producto del carrito.

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Data // genera getters, setters, equals(), hashCode() y toString()
public class CreateOrderRequest {

    @NotEmpty(message = "El pedido debe tener al menos un producto") // la lista no puede estar vacia
    private List<OrderItemRequest> items; // cada elemento es un producto del carrito con su cantidad

    private String couponCode; // codigo de cupon opcional, puede ser null si no se aplica ninguno

    // ---- DATOS DE ENVIO ----
    // @NotBlank rechaza null, cadena vacia "" y cadena solo con espacios "   "

    @NotBlank private String firstName;  // nombre del destinatario
    @NotBlank private String lastName;   // apellido del destinatario
    @NotBlank private String email;      // email de contacto para el envio
    @NotBlank @Pattern(regexp = "^[0-9]{9}$") private String phone;      // telefono: exactamente 9 digitos
    @NotBlank private String address;    // calle y numero
    @NotBlank private String city;       // ciudad
    @NotBlank @Pattern(regexp = "^[0-9]{5}$") private String postalCode; // codigo postal: exactamente 5 digitos
    @NotBlank private String province;   // provincia

    // Clase interna estatica: representa un producto del carrito con su cantidad
    // Es estatica porque no necesita acceder a los campos de CreateOrderRequest para existir
    @Data
    public static class OrderItemRequest {
        private Long productId;    // id del producto que se quiere comprar
        private Integer quantity;  // unidades que el usuario ha puesto en el carrito
    }
}
