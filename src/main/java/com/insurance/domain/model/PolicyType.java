package com.insurance.domain.model;

public enum PolicyType {
    VIDA("Póliza de Vida"),
    VEHICULO("Póliza de Vehículo"),
    SALUD("Póliza de Salud");

    private final String description;

    PolicyType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
