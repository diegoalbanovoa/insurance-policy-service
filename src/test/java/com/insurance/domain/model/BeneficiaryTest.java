package com.insurance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Beneficiary entity class.
 */
@DisplayName("Beneficiary Entity Tests")
class BeneficiaryTest {

    @Test
    @DisplayName("Should create beneficiary using builder")
    void shouldCreateBeneficiaryUsingBuilder() {
        // Given
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        
        // When
        Beneficiary beneficiary = Beneficiary.builder()
                .id(1L)
                .fullName("John Doe")
                .relationship("Spouse")
                .percentage(50.0)
                .birthDate(birthDate)
                .build();
        
        // Then
        assertNotNull(beneficiary);
        assertEquals(1L, beneficiary.getId());
        assertEquals("John Doe", beneficiary.getFullName());
        assertEquals("Spouse", beneficiary.getRelationship());
        assertEquals(50.0, beneficiary.getPercentage());
        assertEquals(birthDate, beneficiary.getBirthDate());
    }

    @Test
    @DisplayName("Should create beneficiary using no-args constructor")
    void shouldCreateBeneficiaryUsingNoArgsConstructor() {
        // When
        Beneficiary beneficiary = new Beneficiary();
        
        // Then
        assertNotNull(beneficiary);
        assertNull(beneficiary.getId());
        assertNull(beneficiary.getFullName());
        assertNull(beneficiary.getRelationship());
        assertNull(beneficiary.getPercentage());
        assertNull(beneficiary.getBirthDate());
    }

    @Test
    @DisplayName("Should create beneficiary using all-args constructor")
    void shouldCreateBeneficiaryUsingAllArgsConstructor() {
        // Given
        Policy policy = Policy.builder().id(1L).build();
        LocalDate birthDate = LocalDate.of(1985, 10, 20);
        
        // When
        Beneficiary beneficiary = new Beneficiary(1L, policy, "Jane Smith", "Child", 30.0, birthDate);
        
        // Then
        assertNotNull(beneficiary);
        assertEquals(1L, beneficiary.getId());
        assertEquals(policy, beneficiary.getPolicy());
        assertEquals("Jane Smith", beneficiary.getFullName());
        assertEquals("Child", beneficiary.getRelationship());
        assertEquals(30.0, beneficiary.getPercentage());
        assertEquals(birthDate, beneficiary.getBirthDate());
    }

    @Test
    @DisplayName("Should set and get policy relationship")
    void shouldSetAndGetPolicyRelationship() {
        // Given
        Beneficiary beneficiary = new Beneficiary();
        Policy policy = Policy.builder()
                .id(100L)
                .policyNumber("POL-2023-001")
                .build();
        
        // When
        beneficiary.setPolicy(policy);
        
        // Then
        assertNotNull(beneficiary.getPolicy());
        assertEquals(100L, beneficiary.getPolicy().getId());
        assertEquals("POL-2023-001", beneficiary.getPolicy().getPolicyNumber());
    }

    @Test
    @DisplayName("Should set all properties using setters")
    void shouldSetAllPropertiesUsingSetters() {
        // Given
        Beneficiary beneficiary = new Beneficiary();
        Policy policy = Policy.builder().id(1L).build();
        LocalDate birthDate = LocalDate.of(2000, 1, 1);
        
        // When
        beneficiary.setId(10L);
        beneficiary.setPolicy(policy);
        beneficiary.setFullName("Alice Johnson");
        beneficiary.setRelationship("Parent");
        beneficiary.setPercentage(25.0);
        beneficiary.setBirthDate(birthDate);
        
        // Then
        assertEquals(10L, beneficiary.getId());
        assertEquals(policy, beneficiary.getPolicy());
        assertEquals("Alice Johnson", beneficiary.getFullName());
        assertEquals("Parent", beneficiary.getRelationship());
        assertEquals(25.0, beneficiary.getPercentage());
        assertEquals(birthDate, beneficiary.getBirthDate());
    }

    @Test
    @DisplayName("Should support equals and hashCode")
    void shouldSupportEqualsAndHashCode() {
        // Given
        Policy policy = Policy.builder().id(1L).build();
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        
        Beneficiary beneficiary1 = Beneficiary.builder()
                .id(1L)
                .policy(policy)
                .fullName("John Doe")
                .relationship("Spouse")
                .percentage(50.0)
                .birthDate(birthDate)
                .build();
        
        Beneficiary beneficiary2 = Beneficiary.builder()
                .id(1L)
                .policy(policy)
                .fullName("John Doe")
                .relationship("Spouse")
                .percentage(50.0)
                .birthDate(birthDate)
                .build();
        
        // Then
        assertEquals(beneficiary1, beneficiary2);
        assertEquals(beneficiary1.hashCode(), beneficiary2.hashCode());
    }

    @Test
    @DisplayName("Should support toString method")
    void shouldSupportToStringMethod() {
        // Given
        LocalDate birthDate = LocalDate.of(1995, 8, 25);
        Beneficiary beneficiary = Beneficiary.builder()
                .id(1L)
                .fullName("Bob Williams")
                .relationship("Sibling")
                .percentage(20.0)
                .birthDate(birthDate)
                .build();
        
        // When
        String toString = beneficiary.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Bob Williams"));
        assertTrue(toString.contains("Sibling"));
    }

    @Test
    @DisplayName("Should handle null birth date")
    void shouldHandleNullBirthDate() {
        // When
        Beneficiary beneficiary = Beneficiary.builder()
                .fullName("Charlie Brown")
                .relationship("Friend")
                .percentage(10.0)
                .birthDate(null)
                .build();
        
        // Then
        assertNull(beneficiary.getBirthDate());
    }

    @Test
    @DisplayName("Should handle various percentage values")
    void shouldHandleVariousPercentageValues() {
        // Given
        double[] percentages = {0.0, 25.0, 50.0, 75.0, 100.0};
        
        // When & Then
        for (double percentage : percentages) {
            Beneficiary beneficiary = Beneficiary.builder()
                    .fullName("Test Beneficiary")
                    .relationship("Test")
                    .percentage(percentage)
                    .build();
            
            assertEquals(percentage, beneficiary.getPercentage());
        }
    }

    @Test
    @DisplayName("Should handle common relationship types")
    void shouldHandleCommonRelationshipTypes() {
        // Given
        String[] relationships = {"Spouse", "Child", "Parent", "Sibling", "Other"};
        
        // When & Then
        for (String relationship : relationships) {
            Beneficiary beneficiary = Beneficiary.builder()
                    .fullName("Test Person")
                    .relationship(relationship)
                    .percentage(20.0)
                    .build();
            
            assertEquals(relationship, beneficiary.getRelationship());
        }
    }

    @Test
    @DisplayName("Should create beneficiary with minimum required fields")
    void shouldCreateBeneficiaryWithMinimumRequiredFields() {
        // When
        Beneficiary beneficiary = Beneficiary.builder()
                .fullName("Minimum Beneficiary")
                .relationship("Relative")
                .percentage(15.0)
                .build();
        
        // Then
        assertNotNull(beneficiary);
        assertNull(beneficiary.getId()); // ID is auto-generated
        assertEquals("Minimum Beneficiary", beneficiary.getFullName());
        assertEquals("Relative", beneficiary.getRelationship());
        assertEquals(15.0, beneficiary.getPercentage());
        assertNull(beneficiary.getBirthDate()); // Optional field
    }
}
