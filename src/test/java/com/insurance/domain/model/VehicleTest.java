package com.insurance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Vehicle entity class.
 */
@DisplayName("Vehicle Entity Tests")
class VehicleTest {

    @Test
    @DisplayName("Should create vehicle using builder")
    void shouldCreateVehicleUsingBuilder() {
        // When
        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .plate("ABC123")
                .brand("Toyota")
                .model("Corolla")
                .year(2022)
                .vehicleType("Sedan")
                .build();
        
        // Then
        assertNotNull(vehicle);
        assertEquals(1L, vehicle.getId());
        assertEquals("ABC123", vehicle.getPlate());
        assertEquals("Toyota", vehicle.getBrand());
        assertEquals("Corolla", vehicle.getModel());
        assertEquals(2022, vehicle.getYear());
        assertEquals("Sedan", vehicle.getVehicleType());
    }

    @Test
    @DisplayName("Should create vehicle using no-args constructor")
    void shouldCreateVehicleUsingNoArgsConstructor() {
        // When
        Vehicle vehicle = new Vehicle();
        
        // Then
        assertNotNull(vehicle);
        assertNull(vehicle.getId());
        assertNull(vehicle.getPlate());
        assertNull(vehicle.getBrand());
        assertNull(vehicle.getModel());
        assertNull(vehicle.getYear());
        assertNull(vehicle.getVehicleType());
    }

    @Test
    @DisplayName("Should create vehicle using all-args constructor")
    void shouldCreateVehicleUsingAllArgsConstructor() {
        // Given
        Policy policy = Policy.builder().id(1L).build();
        
        // When
        Vehicle vehicle = new Vehicle(1L, policy, "XYZ789", "Honda", "Civic", 2023, "Hatchback");
        
        // Then
        assertNotNull(vehicle);
        assertEquals(1L, vehicle.getId());
        assertEquals(policy, vehicle.getPolicy());
        assertEquals("XYZ789", vehicle.getPlate());
        assertEquals("Honda", vehicle.getBrand());
        assertEquals("Civic", vehicle.getModel());
        assertEquals(2023, vehicle.getYear());
        assertEquals("Hatchback", vehicle.getVehicleType());
    }

    @Test
    @DisplayName("Should set and get policy relationship")
    void shouldSetAndGetPolicyRelationship() {
        // Given
        Vehicle vehicle = new Vehicle();
        Policy policy = Policy.builder()
                .id(100L)
                .policyNumber("POL-2023-001")
                .build();
        
        // When
        vehicle.setPolicy(policy);
        
        // Then
        assertNotNull(vehicle.getPolicy());
        assertEquals(100L, vehicle.getPolicy().getId());
        assertEquals("POL-2023-001", vehicle.getPolicy().getPolicyNumber());
    }

    @Test
    @DisplayName("Should set all properties using setters")
    void shouldSetAllPropertiesUsingSetters() {
        // Given
        Vehicle vehicle = new Vehicle();
        Policy policy = Policy.builder().id(1L).build();
        
        // When
        vehicle.setId(10L);
        vehicle.setPolicy(policy);
        vehicle.setPlate("DEF456");
        vehicle.setBrand("Ford");
        vehicle.setModel("Focus");
        vehicle.setYear(2021);
        vehicle.setVehicleType("SUV");
        
        // Then
        assertEquals(10L, vehicle.getId());
        assertEquals(policy, vehicle.getPolicy());
        assertEquals("DEF456", vehicle.getPlate());
        assertEquals("Ford", vehicle.getBrand());
        assertEquals("Focus", vehicle.getModel());
        assertEquals(2021, vehicle.getYear());
        assertEquals("SUV", vehicle.getVehicleType());
    }

    @Test
    @DisplayName("Should support equals and hashCode")
    void shouldSupportEqualsAndHashCode() {
        // Given
        Policy policy = Policy.builder().id(1L).build();
        
        Vehicle vehicle1 = Vehicle.builder()
                .id(1L)
                .policy(policy)
                .plate("AAA111")
                .brand("Toyota")
                .model("Camry")
                .year(2020)
                .vehicleType("Sedan")
                .build();
        
        Vehicle vehicle2 = Vehicle.builder()
                .id(1L)
                .policy(policy)
                .plate("AAA111")
                .brand("Toyota")
                .model("Camry")
                .year(2020)
                .vehicleType("Sedan")
                .build();
        
        // Then
        assertEquals(vehicle1, vehicle2);
        assertEquals(vehicle1.hashCode(), vehicle2.hashCode());
    }

    @Test
    @DisplayName("Should support toString method")
    void shouldSupportToStringMethod() {
        // Given
        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .plate("BBB222")
                .brand("Nissan")
                .model("Sentra")
                .year(2022)
                .vehicleType("Sedan")
                .build();
        
        // When
        String toString = vehicle.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("BBB222"));
        assertTrue(toString.contains("Nissan"));
        assertTrue(toString.contains("Sentra"));
    }

    @Test
    @DisplayName("Should handle null policy")
    void shouldHandleNullPolicy() {
        // When
        Vehicle vehicle = Vehicle.builder()
                .plate("CCC333")
                .brand("Mazda")
                .model("CX-5")
                .year(2023)
                .vehicleType("SUV")
                .policy(null)
                .build();
        
        // Then
        assertNull(vehicle.getPolicy());
    }

    @Test
    @DisplayName("Should create vehicle with minimum required fields")
    void shouldCreateVehicleWithMinimumRequiredFields() {
        // When
        Vehicle vehicle = Vehicle.builder()
                .plate("MIN001")
                .brand("Brand")
                .model("Model")
                .year(2020)
                .vehicleType("Type")
                .build();
        
        // Then
        assertNotNull(vehicle);
        assertNull(vehicle.getId()); // ID is auto-generated
        assertEquals("MIN001", vehicle.getPlate());
        assertEquals("Brand", vehicle.getBrand());
        assertEquals("Model", vehicle.getModel());
        assertEquals(2020, vehicle.getYear());
        assertEquals("Type", vehicle.getVehicleType());
    }

    @Test
    @DisplayName("Should handle different vehicle types")
    void shouldHandleDifferentVehicleTypes() {
        // Given
        String[] vehicleTypes = {"Sedan", "SUV", "Truck", "Van", "Motorcycle", "Hatchback"};
        
        // When & Then
        for (String type : vehicleTypes) {
            Vehicle vehicle = Vehicle.builder()
                    .plate("TYPE" + type.charAt(0))
                    .brand("Brand")
                    .model("Model")
                    .year(2023)
                    .vehicleType(type)
                    .build();
            
            assertEquals(type, vehicle.getVehicleType());
        }
    }
}
