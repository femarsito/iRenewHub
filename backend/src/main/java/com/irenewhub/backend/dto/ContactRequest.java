package com.irenewhub.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    private String phone;

    @NotBlank(message = "El asunto es obligatorio")
    @Size(min = 5, message = "El asunto debe tener al menos 5 caracteres")
    private String subject;

    @NotBlank(message = "El mensaje es obligatorio")
    @Size(min = 20, max = 500, message = "El mensaje debe tener entre 20 y 500 caracteres")
    private String message;
}