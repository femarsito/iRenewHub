package com.irenewhub.backend.dto;

// ---- DTO: CONTACT REQUEST ----
// JSON que llega del frontend cuando un usuario envia un mensaje desde el formulario de contacto.
// No requiere autenticacion: cualquier visitante puede contactar, aunque no este registrado.

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data // genera getters, setters, equals(), hashCode() y toString()
public class ContactRequest {

    @NotBlank(message = "El nombre es obligatorio") // rechaza null, "" y "   "
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email invalido") // valida que tenga formato correcto: x@x.x
    private String email;

    private String phone; // telefono opcional, sin validacion porque puede ser null

    @NotBlank(message = "El asunto es obligatorio")
    @Size(min = 5, message = "El asunto debe tener al menos 5 caracteres")
    private String subject;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 20, max = 500, message = "El mensaje debe tener entre 20 y 500 caracteres")
    private String message;
}
