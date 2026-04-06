package com.irenewhub.backend.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message); //Pasa por la clase padre (super)
    }
}