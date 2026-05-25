package com.irenewhub.backend.controller;

// ---- CONTROLADOR DE PAGOS ----
// Integración con Stripe en modo TEST (no se cobra dinero real).
//
// Flujo:
//   1. El frontend llama a POST /api/payment/create-intent con el importe total
//   2. Este controlador crea un PaymentIntent en Stripe y devuelve el clientSecret
//   3. El frontend usa ese clientSecret con Stripe.js para confirmar el pago
//   4. Stripe valida la tarjeta (en test: 4242 4242 4242 4242 siempre funciona)
//   5. Si el pago es OK, el frontend crea el pedido en nuestro backend

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.publishable.key}")
    private String stripePublishableKey;

    // ---- DEVOLVER LA CLAVE PÚBLICA AL FRONTEND ----
    // La clave pública (pk_test_...) es segura en el frontend — Stripe la usa
    // para mostrar el formulario de tarjeta, no para cobrar.

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getConfig() {
        return ResponseEntity.ok(Map.of("publishableKey", stripePublishableKey));
    }

    // ---- CREAR PAYMENT INTENT ----
    // Un PaymentIntent representa la intención de cobrar cierta cantidad.
    // Stripe devuelve un clientSecret que el frontend usa para confirmar el pago.

    @PostMapping("/create-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @RequestBody Map<String, Object> body) throws StripeException {

        Stripe.apiKey = stripeSecretKey;

        // Stripe trabaja en céntimos (euros × 100)
        double totalEuros = Double.parseDouble(body.get("amount").toString());
        long amountCents  = Math.round(totalEuros * 100);

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountCents)
                .setCurrency("eur")
                .build();

        PaymentIntent intent = PaymentIntent.create(params);

        return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
    }
}
