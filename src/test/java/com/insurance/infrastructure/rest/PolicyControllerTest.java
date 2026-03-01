package com.insurance.infrastructure.rest;

import com.insurance.application.dto.*;
import com.insurance.application.usecase.PolicyService;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import com.insurance.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyController Tests")
public class PolicyControllerTest {

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private PolicyController policyController;

    private PolicyCreateRequest policyRequest;
    private PolicyResponse policyResponse;
    private BeneficiaryCreateRequest beneficiaryRequest;
    private BeneficiaryResponse beneficiaryResponse;
    private VehicleCreateRequest vehicleRequest;
    private VehicleResponse vehicleResponse;

    @BeforeEach
    void setUp() {
        policyRequest = PolicyCreateRequest.builder()
                .policyType("VIDA")
                .clientId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .premiumAmount(new BigDecimal("50.00"))
                .status("ACTIVA")
                .build();

        policyResponse = PolicyResponse.builder()
                .id(1L)
                .policyNumber("POL-20250228-VIDA-00001")
                .policyType("VIDA")
                .clientId(1L)
                .clientName("Juan García")
                .premiumAmount(new BigDecimal("50.00"))
                .beneficiaries(new ArrayList<>())
                .vehicles(new ArrayList<>())
                .dependents(new ArrayList<>())
                .build();

        beneficiaryRequest = BeneficiaryCreateRequest.builder()
                .fullName("Spouse")
                .relationship("Cónyuge")
                .percentage(new BigDecimal("100.00"))
                .birthDate(LocalDate.of(1992, 5, 20))
                .build();

        beneficiaryResponse = BeneficiaryResponse.builder()
                .id(1L)
                .fullName("Spouse")
                .relationship("Cónyuge")
                .build();

        vehicleRequest = VehicleCreateRequest.builder()
                .plate("ABC-1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2023)
                .vehicleType("Sedan")
                .build();

        vehicleResponse = VehicleResponse.builder()
                .id(1L)
                .plate("ABC-1234")
                .brand("Toyota")
                .model("Corolla")
                .build();
    }

    @Test
    @DisplayName("Should create policy successfully")
    void testCreatePolicySuccess() {
        when(policyService.createPolicy(any(PolicyCreateRequest.class)))
                .thenReturn(policyResponse);

        ResponseEntity<PolicyResponse> response = policyController.createPolicy(policyRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("VIDA", response.getBody().getPolicyType());
        verify(policyService, times(1)).createPolicy(any(PolicyCreateRequest.class));
    }

    @Test
    @DisplayName("Should get policy by ID")
    void testGetPolicyByIdSuccess() {
        when(policyService.getPolicyById(1L))
                .thenReturn(policyResponse);

        ResponseEntity<PolicyResponse> response = policyController.getPolicyById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getBody().getId());
        verify(policyService, times(1)).getPolicyById(1L);
    }

    @Test
    @DisplayName("Should get policies by client ID")
    void testGetPoliciesByClientIdSuccess() {
        List<PolicyResponse> policies = Arrays.asList(policyResponse);
        when(policyService.getPoliciesByClientId(1L))
                .thenReturn(policies);

        ResponseEntity<List<PolicyResponse>> response = policyController.getPoliciesByClientId(1L);

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
        verify(policyService, times(1)).getPoliciesByClientId(1L);
    }

    @Test
    @DisplayName("Should get policies by client and type")
    void testGetPoliciesByClientAndTypeSuccess() {
        List<PolicyResponse> policies = Arrays.asList(policyResponse);
        when(policyService.getPoliciesByClientAndType(1L, "VIDA"))
                .thenReturn(policies);

        ResponseEntity<List<PolicyResponse>> response = policyController.getPoliciesByClientAndType(1L, "VIDA");

        assertNotNull(response);
        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should add beneficiary successfully")
    void testAddBeneficiarySuccess() {
        when(policyService.addBeneficiary(1L, beneficiaryRequest))
                .thenReturn(beneficiaryResponse);

        ResponseEntity<BeneficiaryResponse> response = policyController.addBeneficiary(1L, beneficiaryRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Spouse", response.getBody().getFullName());
    }

    @Test
    @DisplayName("Should add vehicle successfully")
    void testAddVehicleSuccess() {
        when(policyService.addVehicle(1L, vehicleRequest))
                .thenReturn(vehicleResponse);

        ResponseEntity<VehicleResponse> response = policyController.addVehicle(1L, vehicleRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("ABC-1234", response.getBody().getPlate());
    }

    @Test
    @DisplayName("Should delete policy successfully")
    void testDeletePolicySuccess() {
        doNothing().when(policyService).deletePolicy(1L);

        ResponseEntity<Void> response = policyController.deletePolicy(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(policyService, times(1)).deletePolicy(1L);
    }

    @Test
    @DisplayName("Should throw BusinessRuleException on duplicate VIDA")
    void testCreatePolicyDuplicate() {
        when(policyService.createPolicy(any(PolicyCreateRequest.class)))
                .thenThrow(new BusinessRuleException("Already has VIDA"));

        assertThrows(BusinessRuleException.class,
                () -> policyController.createPolicy(policyRequest));
    }

    @Test
    @DisplayName("Should throw ValidationException on invalid dates")
    void testCreatePolicyInvalidDates() {
        when(policyService.createPolicy(any(PolicyCreateRequest.class)))
                .thenThrow(new ValidationException("Invalid dates"));

        assertThrows(ValidationException.class,
                () -> policyController.createPolicy(policyRequest));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException on not found")
    void testGetPolicyByIdNotFound() {
        when(policyService.getPolicyById(999L))
                .thenThrow(new ResourceNotFoundException("Póliza", 999L));

        assertThrows(ResourceNotFoundException.class,
                () -> policyController.getPolicyById(999L));
    }

    @Test
    @DisplayName("Should get beneficiaries by policy ID")
    void testGetBeneficiariesSuccess() {
        List<BeneficiaryResponse> beneficiaries = Arrays.asList(beneficiaryResponse);
        when(policyService.getBeneficiariesByPolicyId(1L))
                .thenReturn(beneficiaries);

        ResponseEntity<List<BeneficiaryResponse>> response = policyController.getBeneficiariesByPolicyId(1L);

        assertEquals(1, response.getBody().size());
    }

    @Test
    @DisplayName("Should get vehicles by policy ID")
    void testGetVehiclesSuccess() {
        List<VehicleResponse> vehicles = Arrays.asList(vehicleResponse);
        when(policyService.getVehiclesByPolicyId(1L))
                .thenReturn(vehicles);

        ResponseEntity<List<VehicleResponse>> response = policyController.getVehiclesByPolicyId(1L);

        assertEquals(1, response.getBody().size());
    }
}
