package com.irenewhub.backend.service;

// ---- SERVICIO DE EMAIL ----
// Envía emails transaccionales usando JavaMailSender (Spring Boot Mail).

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remitente;

    // ---- EMAIL DE RECUPERACIÓN DE CONTRASEÑA ----
    // Envía un link con el token. El usuario hace clic y va a /reset-password?token=xxx

    public void enviarEmailRecuperacion(String destinatario, String token) {
        String enlace = "http://localhost:4200/reset-password?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("iRenewHub — Recupera tu contraseña");
        mensaje.setText(
            "Hola,\n\n" +
            "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta iRenewHub.\n\n" +
            "Haz clic en el siguiente enlace para crear una nueva contraseña:\n\n" +
            enlace + "\n\n" +
            "Este enlace es válido durante 1 hora.\n" +
            "Si no solicitaste este cambio, ignora este email.\n\n" +
            "— El equipo de iRenewHub"
        );

        mailSender.send(mensaje);
    }
}
