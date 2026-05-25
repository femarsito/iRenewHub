package com.irenewhub.backend.exception;

// ---- MANEJADOR GLOBAL DE EXCEPCIONES ----
// Intercepta todas las excepciones que se lanzan en cualquier Controller del proyecto.
// Sin esta clase, si ocurre un error Spring devolveria una respuesta poco clara al frontend.
// Con esta clase, cualquier excepcion se convierte en un JSON estructurado con:
//   - timestamp: cuando ocurrio el error
//   - status:    codigo HTTP (404, 500...)
//   - error:     nombre del error
//   - message:   descripcion legible del problema

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice // le dice a Spring que esta clase maneja excepciones de todos los Controllers
public class GlobalExceptionHandler {

    // @ExceptionHandler indica que este metodo se ejecuta cuando se lanza ResourceNotFoundException
    // Se usa cuando algo no existe en la BD (producto, usuario, pedido...)
    // Devuelve HTTP 404 Not Found con el mensaje de error en JSON
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> error = new HashMap<>(); // Map para construir el JSON de respuesta
        error.put("timestamp", LocalDateTime.now().toString()); // momento exacto del error
        error.put("status", 404);                               // codigo HTTP
        error.put("error", "Not Found");                        // tipo de error
        error.put("message", ex.getMessage());                  // mensaje que se paso al lanzar la excepcion
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Captura cualquier otra excepcion no controlada (error generico del servidor)
    // Devuelve HTTP 500 Internal Server Error para que el frontend sepa que algo fallo
    // Es el "red de seguridad": si ningún otro handler lo captura, llega aqui
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", 500);
        error.put("error", "Internal Server Error");
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
