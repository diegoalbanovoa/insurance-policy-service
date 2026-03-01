package com.insurance.infrastructure.rest;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.insurance.application.dto.BeneficiaryCreateRequest;
import com.insurance.application.dto.DependentCreateRequest;
import com.insurance.application.dto.PolicyCreateRequest;
import com.insurance.application.dto.VehicleCreateRequest;
import com.insurance.application.usecase.PolicyService;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyController Unit Tests")
class PolicyControllerTest {

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private PolicyController policyController;

    @Test
    @DisplayName("Should call service when creating policy")
    void testCreatePolicy_CallsService() {
        // Arrange
        PolicyCreateRequest request = PolicyCreateRequest.builder()
            .policyType("VIDA")
            .clientId(1L)
            .premiumAmount(5000.00)
            .build();

        // Act
        policyController.createPolicy(request);

        // Assert
        verify(policyService, times(1)).createPolicy(request);
    }

    @Test
    @DisplayName("Should call service when getting policy by ID")
    void testGetPolicyById_CallsService() {
        // Act
        policyController.getPolicyById(1L);

        // Assert
        verify(policyService, times(1)).getPolicyById(1L);
    }

    @Test
    @DisplayName("Should call service when getting policies by client")
    void testGetPoliciesByClientId_CallsService() {
        // Act
        policyController.getPoliciesByClientId(1L);

        // Assert
        verify(policyService, times(1)).getPoliciesByClientId(1L);
    }

    @Test
    @DisplayName("Should call service when getting policies by client and type")
    void testGetPoliciesByClientAndType_CallsService() {
        // Act
        policyController.getPoliciesByClientAndType(1L, "VIDA");

        // Assert
        verify(policyService, times(1)).getPoliciesByClientAndType(1L, "VIDA");
    }

    @Test
    @DisplayName("Should call service when updating policy status")
    void testUpdatePolicyStatus_CallsService() {
        // Act
        policyController.updatePolicyStatus(1L, "INACTIVA");

        // Assert
        verify(policyService, times(1)).updatePolicyStatus(1L, "INACTIVA");
    }

    @Test
    @DisplayName("Should call service when deleting policy")
    void testDeletePolicy_CallsService() {
        // Act
        policyController.deletePolicy(1L);

        // Assert
        verify(policyService, times(1)).deletePolicy(1L);
    }

    @Test
    @DisplayName("Should call service when adding beneficiary")
    void testAddBeneficiary_CallsService() {
        // Arrange
        BeneficiaryCreateRequest request = BeneficiaryCreateRequest.builder()
            .fullName("María García")
            .relationship("Esposa")
            .percentage(100.00)
            .build();

        // Act
        policyController.addBeneficiary(1L, request);

        // Assert
        verify(policyService, times(1)).addBeneficiary(1L, request);
    }

    @Test
    @DisplayName("Should call service when getting beneficiaries")
    void testGetBeneficiariesByPolicyId_CallsService() {
        // Act
        policyController.getBeneficiariesByPolicyId(1L);

        // Assert
        verify(policyService, times(1)).getBeneficiariesByPolicyId(1L);
    }

    @Test
    @DisplayName("Should call service when adding vehicle")
    void testAddVehicle_CallsService() {
        // Arrange
        VehicleCreateRequest request = VehicleCreateRequest.builder()
            .brand("Toyota")
            .model("Corolla")
            .plate("ABC123")
            .year(2023)
            .build();

        // Act
        policyController.addVehicle(1L, request);

        // Assert
        verify(policyService, times(1)).addVehicle(1L, request);
    }

    @Test
    @DisplayName("Should call service when getting vehicles")
    void testGetVehiclesByPolicyId_CallsService() {
        // Act
        policyController.getVehiclesByPolicyId(1L);

        // Assert
        verify(policyService, times(1)).getVehiclesByPolicyId(1L);
    }

    @Test
    @DisplayName("Should call service when adding dependent")
    void testAddDependent_CallsService() {
        // Arrange
        DependentCreateRequest request = DependentCreateRequest.builder()
            .fullName("Luis Pérez")
            .relationship("Hijo")
            .build();

        // Act
        policyController.addDependent(1L, request);

        // Assert
        verify(policyService, times(1)).addDependent(1L, request);
    }

    @Test
    @DisplayName("Should call service when getting dependents")
    void testGetDependentsByPolicyId_CallsService() {
        // Act
        policyController.getDependentsByPolicyId(1L);

        // Assert
        verify(policyService, times(1)).getDependentsByPolicyId(1L);
    }
}
