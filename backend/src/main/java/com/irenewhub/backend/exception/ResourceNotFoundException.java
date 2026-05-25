package com.irenewhub.backend.exception;

// ---- EXCEPCION: RECURSO NO ENCONTRADO ----
// Excepcion personalizada que se lanza cuando se busca algo en la BD y no existe.
// Por ejemplo: buscar un producto por ID que no existe, o un usuario que no esta registrado.
// Al extender RuntimeException no obliga a capturarla con try/catch en cada sitio:
// si no se captura, la recoge GlobalExceptionHandler y devuelve un error 404 al frontend.

public class ResourceNotFoundException extends RuntimeException {

    // Constructor que recibe el mensaje de error descriptivo
    // Ejemplo: "Producto no encontrado con id: 99"
    public ResourceNotFoundException(String message) {
        super(message); // pasa el mensaje a RuntimeException para que lo almacene internamente
    }
}
