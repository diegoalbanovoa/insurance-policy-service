package com.insurance.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.application.dto.*;
import com.insurance.application.usecase.PolicyService;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import com.insurance.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PolicyController.class)
@DisplayName("PolicyController Tests")
public class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PolicyService policyService;

    private PolicyCreateRequest policyRequest;
    private PolicyResponse policyResponse;
    private BeneficiaryCreateRequest beneficiaryRequest;
    private BeneficiaryResponse beneficiaryResponse;
    private VehicleCreateRequest vehicleRequest;
    private VehicleResponse vehicleResponse;
    private DependentCreateRequest dependentRequest;
    private DependentResponse dependentResponse;

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
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .premiumAmount(new BigDecimal("50.00"))
                .status("ACTIVA")
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
                .percentage(new BigDecimal("100.00"))
                .birthDate(LocalDate.of(1992, 5, 20))
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
                .year(2023)
                .vehicleType("Sedan")
                .build();

        dependentRequest = DependentCreateRequest.builder()
                .fullName("Child")
                .relationship("Hijo/a")
                .birthDate(LocalDate.of(2015, 6, 10))
                .dependentType("Biological")
                .build();

        dependentResponse = DependentResponse.builder()
                .id(1L)
                .fullName("Child")
                .relationship("Hijo/a")
                .birthDate(LocalDate.of(2015, 6, 10))
                .dependentType("Biological")
                .build();
    }

    @Test
    @DisplayName("Should create policy successfully and return 201")
    void testCreatePolicySuccess() throws Exception {
        // Given
        when(policyService.createPolicy(any(PolicyCreateRequest.class)))
                .thenReturn(policyResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.policyType").value("VIDA"))
                .andExpect(jsonPath("$.clientId").value(1));

        verify(policyService, times(1)).createPolicy(any(PolicyCreateRequest.class));
    }

    @Test
    @DisplayName("Should return 409 when creating duplicate VIDA policy")
    void testCreatePolicyDuplicate() throws Exception {
        // Given
        when(policyService.createPolicy(any(PolicyCreateRequest.class)))
                .thenThrow(new BusinessRuleException("Client already has VIDA policy"));

        // When & Then
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(policyService, times(1)).createPolicy(any(PolicyCreateRequest.class));
    }

    @Test
    @DisplayName("Should return 400 for invalid policy dates")
    void testCreatePolicyInvalidDates() throws Exception {
        // Given
        when(policyService.createPolicy(any(PolicyCreateRequest.class)))
                .thenThrow(new ValidationException("Invalid date range"));

        // When & Then
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(policyService, times(1)).createPolicy(any(PolicyCreateRequest.class));
    }

    @Test
    @DisplayName("Should retrieve policy by ID successfully")
    void testGetPolicyByIdSuccess() throws Exception {
        // Given
        when(policyService.getPolicyById(1L))
                .thenReturn(policyResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/policies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.policyType").value("VIDA"));

        verify(policyService, times(1)).getPolicyById(1L);
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent policy")
    void testGetPolicyByIdNotFound() throws Exception {
        // Given
        when(policyService.getPolicyById(999L))
                .thenThrow(new ResourceNotFoundException("Póliza", 999L));

        // When & Then
        mockMvc.perform(get("/api/v1/policies/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(policyService, times(1)).getPolicyById(999L);
    }

    @Test
    @DisplayName("Should retrieve policies by client ID")
    void testGetPoliciesByClientIdSuccess() throws Exception {
        // Given
        List<PolicyResponse> policies = Arrays.asList(policyResponse);
        when(policyService.getPoliciesByClientId(1L))
                .thenReturn(policies);

        // When & Then
        mockMvc.perform(get("/api/v1/policies/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].policyType").value("VIDA"));

        verify(policyService, times(1)).getPoliciesByClientId(1L);
    }

    @Test
    @DisplayName("Should retrieve policies by client and type")
    void testGetPoliciesByClientAndTypeSuccess() throws Exception {
        // Given
        List<PolicyResponse> policies = Arrays.asList(policyResponse);
        when(policyService.getPoliciesByClientAndType(1L, "VIDA"))
                .thenReturn(policies);

        // When & Then
        mockMvc.perform(get("/api/v1/policies/client/1/type/VIDA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].policyType").value("VIDA"));

        verify(policyService, times(1)).getPoliciesByClientAndType(1L, "VIDA");
    }

    @Test
    @DisplayName("Should update policy status successfully")
    void testUpdatePolicyStatusSuccess() throws Exception {
        // Given
        PolicyResponse updatedResponse = policyResponse;
        updatedResponse.setStatus("CANCELADA");
        when(policyService.updatePolicyStatus(1L, "CANCELADA"))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(patch("/api/v1/policies/1/status?newStatus=CANCELADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        verify(policyService, times(1)).updatePolicyStatus(1L, "CANCELADA");
    }

    @Test
    @DisplayName("Should delete policy successfully and return 204")
    void testDeletePolicySuccess() throws Exception {
        // Given
        doNothing().when(policyService).deletePolicy(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/policies/1"))
                .andExpect(status().isNoContent());

        verify(policyService, times(1)).deletePolicy(1L);
    }

    @Test
    @DisplayName("Should add beneficiary successfully and return 201")
    void testAddBeneficiarySuccess() throws Exception {
        // Given
        when(policyService.addBeneficiary(1L, beneficiaryRequest))
                .thenReturn(beneficiaryResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/policies/1/beneficiaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beneficiaryRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Spouse"));

        verify(policyService, times(1)).addBeneficiary(1L, beneficiaryRequest);
    }

    @Test
    @DisplayName("Should return 409 when adding third beneficiary")
    void testAddBeneficiaryLimitExceeded() throws Exception {
        // Given
        when(policyService.addBeneficiary(1L, beneficiaryRequest))
                .thenThrow(new BusinessRuleException("Max 2 beneficiaries per policy"));

        // When & Then
        mockMvc.perform(post("/api/v1/policies/1/beneficiaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beneficiaryRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(policyService, times(1)).addBeneficiary(1L, beneficiaryRequest);
    }

    @Test
    @DisplayName("Should list beneficiaries by policy ID")
    void testGetBeneficiariesByPolicyIdSuccess() throws Exception {
        // Given
        List<BeneficiaryResponse> beneficiaries = Arrays.asList(beneficiaryResponse);
        when(policyService.getBeneficiariesByPolicyId(1L))
                .thenReturn(beneficiaries);

        // When & Then
        mockMvc.perform(get("/api/v1/policies/1/beneficiaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName").value("Spouse"));

        verify(policyService, times(1)).getBeneficiariesByPolicyId(1L);
    }

    @Test
    @DisplayName("Should add vehicle successfully and return 201")
    void testAddVehicleSuccess() throws Exception {
        // Given
        when(policyService.addVehicle(1L, vehicleRequest))
                .thenReturn(vehicleResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/policies/1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.plate").value("ABC-1234"));

        verify(policyService, times(1)).addVehicle(1L, vehicleRequest);
    }

    @Test
    @DisplayName("Should return 409 when adding vehicle with duplicate plate")
    void testAddVehicleDuplicatePlate() throws Exception {
        // Given
        when(policyService.addVehicle(1L, vehicleRequest))
                .thenThrow(new BusinessRuleException("Plate already registered"));

        // When & Then
        mockMvc.perform(post("/api/v1/policies/1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(policyService, times(1)).addVehicle(1L, vehicleRequest);
    }

    @Test
    @DisplayName("Should list vehicles by policy ID")
    void testGetVehiclesByPolicyIdSuccess() throws Exception {
        // Given
        List<VehicleResponse> vehicles = Arrays.asList(vehicleResponse);
        when(policyService.getVehiclesByPolicyId(1L))
                .thenReturn(vehicles);

        // When & Then
        mockMvc.perform(get("/api/v1/policies/1/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].plate").value("ABC-1234"));

        verify(policyService, times(1)).getVehiclesByPolicyId(1L);
    }

    @Test
    @DisplayName("Should add dependent successfully and return 201")
    void testAddDependentSuccess() throws Exception {
        // Given
        when(policyService.addDependent(1L, dependentRequest))
                .thenReturn(dependentResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/policies/1/dependents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dependentRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Child"));

        verify(policyService, times(1)).addDependent(1L, dependentRequest);
    }

    @Test
    @DisplayName("Should return 409 when adding dependent to non-SALUD policy")
    void testAddDependentToNonSaludPolicy() throws Exception {
        // Given
        when(policyService.addDependent(1L, dependentRequest))
                .thenThrow(new BusinessRuleException("Only SALUD policies can have dependents"));

        // When & Then
        mockMvc.perform(post("/api/v1/policies/1/dependents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dependentRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        verify(policyService, times(1)).addDependent(1L, dependentRequest);
    }

    @Test
    @DisplayName("Should list dependents by policy ID")
    void testGetDependentsByPolicyIdSuccess() throws Exception {
        // Given
        List<DependentResponse> dependents = Arrays.asList(dependentResponse);
        when(policyService.getDependentsByPolicyId(1L))
                .thenReturn(dependents);

        // When & Then
        mockMvc.perform(get("/api/v1/policies/1/dependents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fullName").value("Child"));

        verify(policyService, times(1)).getDependentsByPolicyId(1L);
    }

    @Test
    @DisplayName("Should return 400 for invalid policy creation data")
    void testCreatePolicyInvalidData() throws Exception {
        // Given
        String invalidJson = "{ \"policyType\": \"INVALID\" }";

        // When & Then
        mockMvc.perform(post("/api/v1/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(policyService, never()).createPolicy(any());
    }

    @Test
    @DisplayName("Should return 404 when adding beneficiary to non-existent policy")
    void testAddBeneficiaryPolicyNotFound() throws Exception {
        // Given
        when(policyService.addBeneficiary(999L, beneficiaryRequest))
                .thenThrow(new ResourceNotFoundException("Póliza", 999L));

        // When & Then
        mockMvc.perform(post("/api/v1/policies/999/beneficiaries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(beneficiaryRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(policyService, times(1)).addBeneficiary(999L, beneficiaryRequest);
    }

    @Test
    @DisplayName("Should return 404 when adding vehicle to non-existent policy")
    void testAddVehiclePolicyNotFound() throws Exception {
        // Given
        when(policyService.addVehicle(999L, vehicleRequest))
                .thenThrow(new ResourceNotFoundException("Póliza", 999L));

        // When & Then
        mockMvc.perform(post("/api/v1/policies/999/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(vehicleRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(policyService, times(1)).addVehicle(999L, vehicleRequest);
    }
}
