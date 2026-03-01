package com.insurance.shared.exception;

public class ResourceNotFoundException extends InsuranceException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " no encontrado con ID: " + id);
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
