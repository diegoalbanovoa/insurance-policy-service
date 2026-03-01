package com.insurance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Dependent entity class.
 */
@DisplayName("Dependent Entity Tests")
class DependentTest {

    @Test
    @DisplayName("Should create dependent using builder")
    void shouldCreateDependentUsingBuilder() {
        // Given
        LocalDate birthDate = LocalDate.of(2010, 3, 15);
        
        // When
        Dependent dependent = Dependent.builder()
                .id(1L)
                .fullName("Emily Johnson")
                .relationship("Child")
                .birthDate(birthDate)
                .dependentType("Minor")
                .build();
        
        // Then
        assertNotNull(dependent);
        assertEquals(1L, dependent.getId());
        assertEquals("Emily Johnson", dependent.getFullName());
        assertEquals("Child", dependent.getRelationship());
        assertEquals(birthDate, dependent.getBirthDate());
        assertEquals("Minor", dependent.getDependentType());
    }

    @Test
    @DisplayName("Should create dependent using no-args constructor")
    void shouldCreateDependentUsingNoArgsConstructor() {
        // When
        Dependent dependent = new Dependent();
        
        // Then
        assertNotNull(dependent);
        assertNull(dependent.getId());
        assertNull(dependent.getFullName());
        assertNull(dependent.getRelationship());
        assertNull(dependent.getBirthDate());
        assertNull(dependent.getDependentType());
    }

    @Test
    @DisplayName("Should create dependent using all-args constructor")
    void shouldCreateDependentUsingAllArgsConstructor() {
        // Given
        Policy policy = Policy.builder().id(1L).build();
        LocalDate birthDate = LocalDate.of(2015, 7, 20);
        
        // When
        Dependent dependent = new Dependent(1L, policy, "Michael Smith", "Son", birthDate, "Student");
        
        // Then
        assertNotNull(dependent);
        assertEquals(1L, dependent.getId());
        assertEquals(policy, dependent.getPolicy());
        assertEquals("Michael Smith", dependent.getFullName());
        assertEquals("Son", dependent.getRelationship());
        assertEquals(birthDate, dependent.getBirthDate());
        assertEquals("Student", dependent.getDependentType());
    }

    @Test
    @DisplayName("Should set and get policy relationship")
    void shouldSetAndGetPolicyRelationship() {
        // Given
        Dependent dependent = new Dependent();
        Policy policy = Policy.builder()
                .id(100L)
                .policyNumber("POL-2023-001")
                .build();
        
        // When
        dependent.setPolicy(policy);
        
        // Then
        assertNotNull(dependent.getPolicy());
        assertEquals(100L, dependent.getPolicy().getId());
        assertEquals("POL-2023-001", dependent.getPolicy().getPolicyNumber());
    }

    @Test
    @DisplayName("Should set all properties using setters")
    void shouldSetAllPropertiesUsingSetters() {
        // Given
        Dependent dependent = new Dependent();
        Policy policy = Policy.builder().id(1L).build();
        LocalDate birthDate = LocalDate.of(2012, 11, 5);
        
        // When
        dependent.setId(10L);
        dependent.setPolicy(policy);
        dependent.setFullName("Sarah Williams");
        dependent.setRelationship("Daughter");
        dependent.setBirthDate(birthDate);
        dependent.setDependentType("Minor");
        
        // Then
        assertEquals(10L, dependent.getId());
        assertEquals(policy, dependent.getPolicy());
        assertEquals("Sarah Williams", dependent.getFullName());
        assertEquals("Daughter", dependent.getRelationship());
        assertEquals(birthDate, dependent.getBirthDate());
        assertEquals("Minor", dependent.getDependentType());
    }

    @Test
    @DisplayName("Should support equals and hashCode")
    void shouldSupportEqualsAndHashCode() {
        // Given
        Policy policy = Policy.builder().id(1L).build();
        LocalDate birthDate = LocalDate.of(2010, 3, 15);
        
        Dependent dependent1 = Dependent.builder()
                .id(1L)
                .policy(policy)
                .fullName("Emily Johnson")
                .relationship("Child")
                .birthDate(birthDate)
                .dependentType("Minor")
                .build();
        
        Dependent dependent2 = Dependent.builder()
                .id(1L)
                .policy(policy)
                .fullName("Emily Johnson")
                .relationship("Child")
                .birthDate(birthDate)
                .dependentType("Minor")
                .build();
        
        // Then
        assertEquals(dependent1, dependent2);
        assertEquals(dependent1.hashCode(), dependent2.hashCode());
    }

    @Test
    @DisplayName("Should support toString method")
    void shouldSupportToStringMethod() {
        // Given
        LocalDate birthDate = LocalDate.of(2013, 6, 10);
        Dependent dependent = Dependent.builder()
                .id(1L)
                .fullName("David Brown")
                .relationship("Son")
                .birthDate(birthDate)
                .dependentType("Student")
                .build();
        
        // When
        String toString = dependent.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("David Brown"));
        assertTrue(toString.contains("Son"));
    }

    @Test
    @DisplayName("Should handle different dependent types")
    void shouldHandleDifferentDependentTypes() {
        // Given
        String[] dependentTypes = {"Minor", "Student", "Disabled", "Elderly", "Unemployed"};
        
        // When & Then
        for (String type : dependentTypes) {
            Dependent dependent = Dependent.builder()
                    .fullName("Test Person")
                    .relationship("Child")
                    .birthDate(LocalDate.of(2010, 1, 1))
                    .dependentType(type)
                    .build();
            
            assertEquals(type, dependent.getDependentType());
        }
    }

    @Test
    @DisplayName("Should handle common relationship types")
    void shouldHandleCommonRelationshipTypes() {
        // Given
        String[] relationships = {"Child", "Son", "Daughter", "Spouse", "Parent", "Sibling"};
        
        // When & Then
        for (String relationship : relationships) {
            Dependent dependent = Dependent.builder()
                    .fullName("Test Dependent")
                    .relationship(relationship)
                    .birthDate(LocalDate.of(2015, 1, 1))
                    .dependentType("Minor")
                    .build();
            
            assertEquals(relationship, dependent.getRelationship());
        }
    }

    @Test
    @DisplayName("Should create dependent with all required fields")
    void shouldCreateDependentWithAllRequiredFields() {
        // Given
        LocalDate birthDate = LocalDate.of(2018, 9, 25);
        
        // When
        Dependent dependent = Dependent.builder()
                .fullName("Required Fields Dependent")
                .relationship("Child")
                .birthDate(birthDate)
                .dependentType("Minor")
                .build();
        
        // Then
        assertNotNull(dependent);
        assertNull(dependent.getId()); // ID is auto-generated
        assertEquals("Required Fields Dependent", dependent.getFullName());
        assertEquals("Child", dependent.getRelationship());
        assertEquals(birthDate, dependent.getBirthDate());
        assertEquals("Minor", dependent.getDependentType());
    }

    @Test
    @DisplayName("Should handle different birth dates")
    void shouldHandleDifferentBirthDates() {
        // Given
        LocalDate[] birthDates = {
            LocalDate.of(2000, 1, 1),
            LocalDate.of(2010, 12, 31),
            LocalDate.of(2020, 6, 15),
            LocalDate.now()
        };
        
        // When & Then
        for (LocalDate birthDate : birthDates) {
            Dependent dependent = Dependent.builder()
                    .fullName("Test Person")
                    .relationship("Child")
                    .birthDate(birthDate)
                    .dependentType("Minor")
                    .build();
            
            assertEquals(birthDate, dependent.getBirthDate());
        }
    }

    @Test
    @DisplayName("Should handle null policy")
    void shouldHandleNullPolicy() {
        // When
        Dependent dependent = Dependent.builder()
                .fullName("No Policy Dependent")
                .relationship("Child")
                .birthDate(LocalDate.of(2015, 1, 1))
                .dependentType("Minor")
                .policy(null)
                .build();
        
        // Then
        assertNull(dependent.getPolicy());
    }
}
