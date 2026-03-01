package com.insurance.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Policy entity class.
 */
@DisplayName("Policy Entity Tests")
class PolicyTest {

    private Client testClient;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .nombres("John")
                .apellidos("Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    @DisplayName("Should create policy using builder")
    void shouldCreatePolicyUsingBuilder() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        // When
        Policy policy = Policy.builder()
                .id(1L)
                .policyNumber("POL-2023-001")
                .policyType(PolicyType.VEHICULO)
                .client(testClient)
                .startDate(startDate)
                .endDate(endDate)
                .premiumAmount(1500.0)
                .status("ACTIVE")
                .build();
        
        // Then
        assertNotNull(policy);
        assertEquals(1L, policy.getId());
        assertEquals("POL-2023-001", policy.getPolicyNumber());
        assertEquals(PolicyType.VEHICULO, policy.getPolicyType());
        assertEquals(testClient, policy.getClient());
        assertEquals(startDate, policy.getStartDate());
        assertEquals(endDate, policy.getEndDate());
        assertEquals(1500.0, policy.getPremiumAmount());
        assertEquals("ACTIVE", policy.getStatus());
    }

    @Test
    @DisplayName("Should create policy using no-args constructor")
    void shouldCreatePolicyUsingNoArgsConstructor() {
        // When
        Policy policy = new Policy();
        
        // Then
        assertNotNull(policy);
        assertNull(policy.getId());
        assertNull(policy.getPolicyNumber());
        assertNull(policy.getPolicyType());
        assertNull(policy.getClient());
    }

    @Test
    @DisplayName("Should initialize empty lists for beneficiaries vehicles and dependents")
    void shouldInitializeEmptyListsForBeneficiariesVehiclesAndDependents() {
        // When
        Policy policy = Policy.builder()
                .policyNumber("POL-2023-002")
                .build();
        
        // Then
        assertNotNull(policy.getBeneficiaries());
        assertNotNull(policy.getVehicles());
        assertNotNull(policy.getDependents());
        assertTrue(policy.getBeneficiaries().isEmpty());
        assertTrue(policy.getVehicles().isEmpty());
        assertTrue(policy.getDependents().isEmpty());
    }

    @Test
    @DisplayName("Should add beneficiaries to policy")
    void shouldAddBeneficiariesToPolicy() {
        // Given
        Policy policy = Policy.builder()
                .policyNumber("POL-2023-003")
                .build();
        
        Beneficiary beneficiary1 = Beneficiary.builder()
                .fullName("Jane Doe")
                .relationship("Spouse")
                .percentage(50.0)
                .build();
        
        Beneficiary beneficiary2 = Beneficiary.builder()
                .fullName("Jim Doe")
                .relationship("Child")
                .percentage(50.0)
                .build();
        
        // When
        policy.getBeneficiaries().add(beneficiary1);
        policy.getBeneficiaries().add(beneficiary2);
        
        // Then
        assertEquals(2, policy.getBeneficiaries().size());
        assertTrue(policy.getBeneficiaries().contains(beneficiary1));
        assertTrue(policy.getBeneficiaries().contains(beneficiary2));
    }

    @Test
    @DisplayName("Should add vehicles to policy")
    void shouldAddVehiclesToPolicy() {
        // Given
        Policy policy = Policy.builder()
                .policyNumber("POL-2023-004")
                .policyType(PolicyType.VEHICULO)
                .build();
        
        Vehicle vehicle = Vehicle.builder()
                .plate("ABC123")
                .brand("Toyota")
                .model("Corolla")
                .year(2023)
                .vehicleType("Sedan")
                .build();
        
        // When
        policy.getVehicles().add(vehicle);
        
        // Then
        assertEquals(1, policy.getVehicles().size());
        assertTrue(policy.getVehicles().contains(vehicle));
    }

    @Test
    @DisplayName("Should add dependents to policy")
    void shouldAddDependentsToPolicy() {
        // Given
        Policy policy = Policy.builder()
                .policyNumber("POL-2023-005")
                .policyType(PolicyType.SALUD)
                .build();
        
        Dependent dependent = Dependent.builder()
                .fullName("Child Doe")
                .relationship("Son")
                .birthDate(LocalDate.of(2015, 5, 10))
                .dependentType("Minor")
                .build();
        
        // When
        policy.getDependents().add(dependent);
        
        // Then
        assertEquals(1, policy.getDependents().size());
        assertTrue(policy.getDependents().contains(dependent));
    }

    @Test
    @DisplayName("Should call onCreate when PrePersist is triggered")
    void shouldCallOnCreateWhenPrePersistIsTriggered() throws Exception {
        // Given
        Policy policy = new Policy();
        Method onCreateMethod = Policy.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        
        // When
        onCreateMethod.invoke(policy);
        
        // Then
        assertNotNull(policy.getCreatedAt());
        assertNotNull(policy.getUpdatedAt());
        assertEquals(policy.getCreatedAt(), policy.getUpdatedAt());
    }

    @Test
    @DisplayName("Should call onUpdate when PreUpdate is triggered")
    void shouldCallOnUpdateWhenPreUpdateIsTriggered() throws Exception {
        // Given
        Policy policy = new Policy();
        Method onCreateMethod = Policy.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(policy);
        
        LocalDateTime originalCreatedAt = policy.getCreatedAt();
        LocalDateTime originalUpdatedAt = policy.getUpdatedAt();
        
        Thread.sleep(10); // Small delay to ensure different timestamp
        
        Method onUpdateMethod = Policy.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        
        // When
        onUpdateMethod.invoke(policy);
        
        // Then
        assertEquals(originalCreatedAt, policy.getCreatedAt()); // createdAt should not change
        assertNotEquals(originalUpdatedAt, policy.getUpdatedAt()); // updatedAt should change
        assertTrue(policy.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should support equals and hashCode")
    void shouldSupportEqualsAndHashCode() {
        // Given
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 1);
        
        Policy policy1 = Policy.builder()
                .id(1L)
                .policyNumber("POL-2023-001")
                .policyType(PolicyType.VEHICULO)
                .client(testClient)
                .startDate(startDate)
                .endDate(endDate)
                .premiumAmount(1500.0)
                .status("ACTIVE")
                .build();
        
        Policy policy2 = Policy.builder()
                .id(1L)
                .policyNumber("POL-2023-001")
                .policyType(PolicyType.VEHICULO)
                .client(testClient)
                .startDate(startDate)
                .endDate(endDate)
                .premiumAmount(1500.0)
                .status("ACTIVE")
                .build();
        
        // Then
        assertEquals(policy1, policy2);
        assertEquals(policy1.hashCode(), policy2.hashCode());
    }

    @Test
    @DisplayName("Should support toString method")
    void shouldSupportToStringMethod() {
        // Given
        Policy policy = Policy.builder()
                .policyNumber("POL-2023-006")
                .policyType(PolicyType.VIDA)
                .status("ACTIVE")
                .build();
        
        // When
        String toString = policy.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("POL-2023-006"));
        assertTrue(toString.contains("VIDA"));
    }

    @Test
    @DisplayName("Should handle different policy types")
    void shouldHandleDifferentPolicyTypes() {
        // Given
        PolicyType[] types = PolicyType.values();
        
        // When & Then
        for (PolicyType type : types) {
            Policy policy = Policy.builder()
                    .policyNumber("POL-" + type.name())
                    .policyType(type)
                    .build();
            
            assertEquals(type, policy.getPolicyType());
        }
    }

    @Test
    @DisplayName("Should handle different policy statuses")
    void shouldHandleDifferentPolicyStatuses() {
        // Given
        String[] statuses = {"ACTIVE", "INACTIVE", "PENDING", "CANCELLED", "EXPIRED"};
        
        // When & Then
        for (String status : statuses) {
            Policy policy = Policy.builder()
                    .policyNumber("POL-STATUS-" + status)
                    .status(status)
                    .build();
            
            assertEquals(status, policy.getStatus());
        }
    }

    @Test
    @DisplayName("Should set all properties using setters")
    void shouldSetAllPropertiesUsingSetters() {
        // Given
        Policy policy = new Policy();
        LocalDate startDate = LocalDate.of(2023, 6, 1);
        LocalDate endDate = LocalDate.of(2024, 6, 1);
        
        //When
        policy.setId(100L);
        policy.setPolicyNumber("POL-SET-001");
        policy.setPolicyType(PolicyType.SALUD);
        policy.setClient(testClient);
        policy.setStartDate(startDate);
        policy.setEndDate(endDate);
        policy.setPremiumAmount(2000.0);
        policy.setStatus("ACTIVE");
        
        // Then
        assertEquals(100L, policy.getId());
        assertEquals("POL-SET-001", policy.getPolicyNumber());
        assertEquals(PolicyType.SALUD, policy.getPolicyType());
        assertEquals(testClient, policy.getClient());
        assertEquals(startDate, policy.getStartDate());
        assertEquals(endDate, policy.getEndDate());
        assertEquals(2000.0, policy.getPremiumAmount());
        assertEquals("ACTIVE", policy.getStatus());
    }

    @Test
    @DisplayName("Should support toBuilder for copying with modifications")
    void shouldSupportToBuilderForCopyingWithModifications() {
        // Given
        Policy original = Policy.builder()
                .id(1L)
                .policyNumber("POL-ORIGINAL")
                .policyType(PolicyType.VEHICULO)
                .status("ACTIVE")
                .premiumAmount(1000.0)
                .build();
        
        // When
        Policy modified = original.toBuilder()
                .premiumAmount(1500.0)
                .status("RENEWED")
                .build();
        
        // Then
        assertEquals(original.getId(), modified.getId());
        assertEquals(original.getPolicyNumber(), modified.getPolicyNumber());
        assertEquals(original.getPolicyType(), modified.getPolicyType());
        assertEquals(1500.0, modified.getPremiumAmount()); // Modified
        assertEquals("RENEWED", modified.getStatus()); // Modified
    }
}
