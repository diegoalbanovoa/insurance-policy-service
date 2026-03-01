package com.insurance.application.usecase;

import com.insurance.application.dto.*;
import com.insurance.domain.model.*;
import com.insurance.domain.repository.*;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyService Tests")
public class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DependentRepository dependentRepository;

    @InjectMocks
    private PolicyService policyService;

    private Client testClient;
    private PolicyCreateRequest policyRequest;
    private Policy existingPolicy;

    @BeforeEach
    void setUp() {
        testClient = Client.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan")
                .apellidos("García")
                .email("juan@example.com")
                .telefono("3101234567")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();

        policyRequest = PolicyCreateRequest.builder()
                .policyType("VIDA")
                .clientId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .premiumAmount(new BigDecimal("50.00"))
                .status("ACTIVA")
                .build();

        existingPolicy = Policy.builder()
                .id(1L)
                .policyNumber("POL-20250228-VIDA-00001")
                .policyType(PolicyType.VIDA)
                .client(testClient)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .premiumAmount(new BigDecimal("50.00"))
                .status("ACTIVA")
                .beneficiaries(new ArrayList<>())
                .vehicles(new ArrayList<>())
                .dependents(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Should create policy successfully for VIDA when no existing policy")
    void testCreateVidaPolicySuccess() {
        // Given
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(testClient));
        when(policyRepository.countByClientIdAndPolicyType(1L, PolicyType.VIDA))
                .thenReturn(0);
        when(policyRepository.save(any(Policy.class)))
                .thenReturn(existingPolicy);

        // When
        PolicyResponse response = policyService.createPolicy(policyRequest);

        // Then
        assertNotNull(response);
        assertEquals("VIDA", response.getPolicyType());
        assertEquals(1L, response.getClientId());
        
        verify(clientRepository, times(1)).findById(1L);
        verify(policyRepository, times(1)).countByClientIdAndPolicyType(1L, PolicyType.VIDA);
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client not found")
    void testCreatePolicyClientNotFound() {
        // Given
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> policyService.createPolicy(policyRequest),
                "Should throw ResourceNotFoundException for missing client");

        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when start date is after end date")
    void testCreatePolicyInvalidDates() {
        // Given
        PolicyCreateRequest invalidRequest = PolicyCreateRequest.builder()
                .policyType("VIDA")
                .clientId(1L)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .premiumAmount(new BigDecimal("50.00"))
                .status("ACTIVA")
                .build();

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(testClient));

        // When & Then
        assertThrows(ValidationException.class,
                () -> policyService.createPolicy(invalidRequest));

        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException when start date equals end date")
    void testCreatePolicyEqualDates() {
        // Given
        PolicyCreateRequest invalidRequest = PolicyCreateRequest.builder()
                .policyType("VIDA")
                .clientId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 1, 1))
                .premiumAmount(new BigDecimal("50.00"))
                .status("ACTIVA")
                .build();

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(testClient));

        // When & Then
        assertThrows(ValidationException.class,
                () -> policyService.createPolicy(invalidRequest));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when client already has VIDA policy")
    void testCreateVidaPolicyDuplicate() {
        // Given
        PolicyCreateRequest vidaRequest = PolicyCreateRequest.builder()
                .policyType("VIDA")
                .clientId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .premiumAmount(new BigDecimal("50.00"))
                .status("ACTIVA")
                .build();

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(testClient));
        when(policyRepository.countByClientIdAndPolicyType(1L, PolicyType.VIDA))
                .thenReturn(1); // Already has 1 VIDA policy

        // When & Then
        assertThrows(BusinessRuleException.class,
                () -> policyService.createPolicy(vidaRequest),
                "Should throw BusinessRuleException for duplicate VIDA policy");

        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create VEHICULO policy successfully")
    void testCreateVehiculoPolicySuccess() {
        // Given
        PolicyCreateRequest vehiculoRequest = PolicyCreateRequest.builder()
                .policyType("VEHICULO")
                .clientId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .premiumAmount(new BigDecimal("100.00"))
                .status("ACTIVA")
                .build();

        Policy vehiculoPolicy = Policy.builder()
                .id(2L)
                .policyNumber("POL-20250228-VEHICULO-00002")
                .policyType(PolicyType.VEHICULO)
                .client(testClient)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2026, 1, 1))
                .premiumAmount(new BigDecimal("100.00"))
                .status("ACTIVA")
                .build();

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(testClient));
        when(policyRepository.save(any(Policy.class)))
                .thenReturn(vehiculoPolicy);

        // When
        PolicyResponse response = policyService.createPolicy(vehiculoRequest);

        // Then
        assertNotNull(response);
        assertEquals("VEHICULO", response.getPolicyType());
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    @DisplayName("Should retrieve policy by ID successfully")
    void testGetPolicyByIdSuccess() {
        // Given
        when(policyRepository.findById(1L))
                .thenReturn(Optional.of(existingPolicy));

        // When
        PolicyResponse response = policyService.getPolicyById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("VIDA", response.getPolicyType());
        
        verify(policyRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when policy not found")
    void testGetPolicyByIdNotFound() {
        // Given
        when(policyRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> policyService.getPolicyById(999L));
    }

    @Test
    @DisplayName("Should retrieve policies by client ID")
    void testGetPoliciesByClientId() {
        // Given
        List<Policy> policies = Arrays.asList(existingPolicy);
        when(clientRepository.existsById(1L))
                .thenReturn(true);
        when(policyRepository.findByClientId(1L))
                .thenReturn(policies);

        // When
        List<PolicyResponse> responses = policyService.getPoliciesByClientId(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("VIDA", responses.get(0).getPolicyType());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client not found in getPoliciesByClientId")
    void testGetPoliciesByClientIdNotFound() {
        // Given
        when(clientRepository.existsById(999L))
                .thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> policyService.getPoliciesByClientId(999L));
    }

    @Test
    @DisplayName("Should add beneficiary to VIDA policy successfully")
    void testAddBeneficiarySuccess() {
        // Given
        BeneficiaryCreateRequest beneficiaryRequest = BeneficiaryCreateRequest.builder()
                .fullName("Spouse")
                .relationship("Cónyuge")
                .percentage(new BigDecimal("100.00"))
                .birthDate(LocalDate.of(1992, 5, 20))
                .build();

        Beneficiary savedBeneficiary = Beneficiary.builder()
                .id(1L)
                .policy(existingPolicy)
                .fullName("Spouse")
                .relationship("Cónyuge")
                .percentage(new BigDecimal("100.00"))
                .birthDate(LocalDate.of(1992, 5, 20))
                .build();

        when(policyRepository.findById(1L))
                .thenReturn(Optional.of(existingPolicy));
        when(beneficiaryRepository.countByPolicyId(1L))
                .thenReturn(0);
        when(beneficiaryRepository.save(any(Beneficiary.class)))
                .thenReturn(savedBeneficiary);

        // When
        BeneficiaryResponse response = policyService.addBeneficiary(1L, beneficiaryRequest);

        // Then
        assertNotNull(response);
        assertEquals("Spouse", response.getFullName());
        verify(beneficiaryRepository, times(1)).save(any(Beneficiary.class));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when adding beneficiary to non-VIDA policy")
    void testAddBeneficiaryToNonVidaPolicyFails() {
        // Given
        Policy vehiculoPolicy = Policy.builder()
                .id(2L)
                .policyType(PolicyType.VEHICULO)
                .client(testClient)
                .build();

        BeneficiaryCreateRequest beneficiaryRequest = BeneficiaryCreateRequest.builder()
                .fullName("Spouse")
                .relationship("Cónyuge")
                .percentage(new BigDecimal("100.00"))
                .birthDate(LocalDate.of(1992, 5, 20))
                .build();

        when(policyRepository.findById(2L))
                .thenReturn(Optional.of(vehiculoPolicy));

        // When & Then
        assertThrows(BusinessRuleException.class,
                () -> policyService.addBeneficiary(2L, beneficiaryRequest),
                "Should fail to add beneficiary to VEHICULO policy");

        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when adding third beneficiary")
    void testAddBeneficiaryLimitExceeded() {
        // Given
        BeneficiaryCreateRequest beneficiaryRequest = BeneficiaryCreateRequest.builder()
                .fullName("Third")
                .relationship("Other")
                .percentage(new BigDecimal("50.00"))
                .birthDate(LocalDate.of(2000, 1, 1))
                .build();

        when(policyRepository.findById(1L))
                .thenReturn(Optional.of(existingPolicy));
        when(beneficiaryRepository.countByPolicyId(1L))
                .thenReturn(2); // Already has 2 beneficiaries

        // When & Then
        assertThrows(BusinessRuleException.class,
                () -> policyService.addBeneficiary(1L, beneficiaryRequest),
                "Should fail when trying to add third beneficiary");

        verify(beneficiaryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should add vehicle to VEHICULO policy successfully")
    void testAddVehicleSuccess() {
        // Given
        Policy vehiculoPolicy = Policy.builder()
                .id(2L)
                .policyType(PolicyType.VEHICULO)
                .client(testClient)
                .vehicles(new ArrayList<>())
                .build();

        VehicleCreateRequest vehicleRequest = VehicleCreateRequest.builder()
                .plate("ABC-1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2023)
                .vehicleType("Sedan")
                .build();

        Vehicle savedVehicle = Vehicle.builder()
                .id(1L)
                .policy(vehiculoPolicy)
                .plate("ABC-1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2023)
                .vehicleType("Sedan")
                .build();

        when(policyRepository.findById(2L))
                .thenReturn(Optional.of(vehiculoPolicy));
        when(vehicleRepository.findByPlate("ABC-1234"))
                .thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class)))
                .thenReturn(savedVehicle);

        // When
        VehicleResponse response = policyService.addVehicle(2L, vehicleRequest);

        // Then
        assertNotNull(response);
        assertEquals("ABC-1234", response.getPlate());
        verify(vehicleRepository, times(1)).save(any(Vehicle.class));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when plate already registered")
    void testAddVehicleDuplicatePlate() {
        // Given
        Policy vehiculoPolicy = Policy.builder()
                .id(2L)
                .policyType(PolicyType.VEHICULO)
                .client(testClient)
                .build();

        VehicleCreateRequest vehicleRequest = VehicleCreateRequest.builder()
                .plate("ABC-1234")
                .brand("Toyota")
                .model("Corolla")
                .year(2023)
                .vehicleType("Sedan")
                .build();

        Vehicle existingVehicle = Vehicle.builder()
                .id(1L)
                .plate("ABC-1234")
                .build();

        when(policyRepository.findById(2L))
                .thenReturn(Optional.of(vehiculoPolicy));
        when(vehicleRepository.findByPlate("ABC-1234"))
                .thenReturn(Optional.of(existingVehicle));

        // When & Then
        assertThrows(BusinessRuleException.class,
                () -> policyService.addVehicle(2L, vehicleRequest),
                "Should fail for duplicate plate");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should add dependent to SALUD policy successfully")
    void testAddDependentSuccess() {
        // Given
        Policy saludPolicy = Policy.builder()
                .id(3L)
                .policyType(PolicyType.SALUD)
                .client(testClient)
                .dependents(new ArrayList<>())
                .build();

        DependentCreateRequest dependentRequest = DependentCreateRequest.builder()
                .fullName("Child")
                .relationship("Hijo/a")
                .birthDate(LocalDate.of(2015, 6, 10))
                .dependentType("Biological")
                .build();

        Dependent savedDependent = Dependent.builder()
                .id(1L)
                .policy(saludPolicy)
                .fullName("Child")
                .relationship("Hijo/a")
                .birthDate(LocalDate.of(2015, 6, 10))
                .dependentType("Biological")
                .build();

        when(policyRepository.findById(3L))
                .thenReturn(Optional.of(saludPolicy));
        when(dependentRepository.save(any(Dependent.class)))
                .thenReturn(savedDependent);

        // When
        DependentResponse response = policyService.addDependent(3L, dependentRequest);

        // Then
        assertNotNull(response);
        assertEquals("Child", response.getFullName());
        verify(dependentRepository, times(1)).save(any(Dependent.class));
    }

    @Test
    @DisplayName("Should update policy status successfully")
    void testUpdatePolicyStatusSuccess() {
        // Given
        when(policyRepository.findById(1L))
                .thenReturn(Optional.of(existingPolicy));
        when(policyRepository.save(any(Policy.class)))
                .thenReturn(existingPolicy);

        // When
        PolicyResponse response = policyService.updatePolicyStatus(1L, "CANCELADA");

        // Then
        assertNotNull(response);
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    @DisplayName("Should delete policy successfully")
    void testDeletePolicySuccess() {
        // Given
        when(policyRepository.existsById(1L))
                .thenReturn(true);

        // When
        policyService.deletePolicy(1L);

        // Then
        verify(policyRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent policy")
    void testDeletePolicyNotFound() {
        // Given
        when(policyRepository.existsById(999L))
                .thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> policyService.deletePolicy(999L));

        verify(policyRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should list beneficiaries by policy ID")
    void testGetBeneficiariesByPolicyId() {
        // Given
        Beneficiary beneficiary = Beneficiary.builder()
                .id(1L)
                .fullName("Spouse")
                .relationship("Cónyuge")
                .percentage(new BigDecimal("100.00"))
                .build();

        when(policyRepository.existsById(1L))
                .thenReturn(true);
        when(beneficiaryRepository.findByPolicyId(1L))
                .thenReturn(Arrays.asList(beneficiary));

        // When
        List<BeneficiaryResponse> responses = policyService.getBeneficiariesByPolicyId(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Spouse", responses.get(0).getFullName());
    }

    @Test
    @DisplayName("Should list vehicles by policy ID")
    void testGetVehiclesByPolicyId() {
        // Given
        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .plate("ABC-1234")
                .brand("Toyota")
                .model("Corolla")
                .build();

        when(policyRepository.existsById(1L))
                .thenReturn(true);
        when(vehicleRepository.findByPolicyId(1L))
                .thenReturn(Arrays.asList(vehicle));

        // When
        List<VehicleResponse> responses = policyService.getVehiclesByPolicyId(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("ABC-1234", responses.get(0).getPlate());
    }

    @Test
    @DisplayName("Should list dependents by policy ID")
    void testGetDependentsByPolicyId() {
        // Given
        Dependent dependent = Dependent.builder()
                .id(1L)
                .fullName("Child")
                .relationship("Hijo/a")
                .dependentType("Biological")
                .build();

        when(policyRepository.existsById(1L))
                .thenReturn(true);
        when(dependentRepository.findByPolicyId(1L))
                .thenReturn(Arrays.asList(dependent));

        // When
        List<DependentResponse> responses = policyService.getDependentsByPolicyId(1L);

        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("Child", responses.get(0).getFullName());
    }
}
