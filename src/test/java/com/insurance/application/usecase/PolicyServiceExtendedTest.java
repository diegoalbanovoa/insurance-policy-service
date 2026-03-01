package com.insurance.application.usecase;

import com.insurance.domain.model.Client;
import com.insurance.domain.model.Policy;
import com.insurance.domain.model.PolicyType;
import com.insurance.domain.repository.ClientRepository;
import com.insurance.domain.repository.PolicyRepository;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyService Extended Tests")
class PolicyServiceExtendedTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private ClientRepository clientRepository;

    private PolicyService policyService;

    private Policy testPolicy;
    private Client testClient;

    @BeforeEach
    void setUp() {
        policyService = new PolicyService(policyRepository, clientRepository);

        testClient = Client.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan")
                .apellidos("Pérez García")
                .email("juan.perez@example.com")
                .telefono("+57 3001234567")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .build();

        testPolicy = Policy.builder()
                .id(1L)
                .policyNo("POL-001")
                .type(PolicyType.VIDA)
                .client(testClient)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(1))
                .premium(new BigDecimal("500000"))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when getting non-existent policy")
    void testGetPolicyById_NotFound() {
        // Arrange
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            policyService.getPolicyById(999L);
        });
    }

    @Test
    @DisplayName("Should throw BusinessRuleException on duplicate VIDA policy")
    void testCreatePolicy_DuplicateVidaPolicy() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(policyRepository.findByClientIdAndType(1L, PolicyType.VIDA))
                .thenReturn(Arrays.asList(testPolicy));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            policyService.createPolicy(testPolicy);
        });
        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when creating policy for non-existent client")
    void testCreatePolicy_ClientNotFound() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            policyService.createPolicy(testPolicy);
        });
        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should list policies by client successfully")
    void testListPoliciesByClientId_Success() {
        // Arrange
        Policy policy2 = Policy.builder()
                .id(2L)
                .policyNo("POL-002")
                .type(PolicyType.VEHICULO)
                .client(testClient)
                .premium(new BigDecimal("750000"))
                .status("ACTIVE")
                .build();

        when(policyRepository.findByClientId(1L))
                .thenReturn(Arrays.asList(testPolicy, policy2));

        // Act
        var policies = policyService.listPoliciesByClientId(1L);

        // Assert
        assertEquals(2, policies.size());
        assertEquals(PolicyType.VIDA, policies.get(0).getType());
        assertEquals(PolicyType.VEHICULO, policies.get(1).getType());
    }

    @Test
    @DisplayName("Should return empty list when client has no policies")
    void testListPoliciesByClientId_Empty() {
        // Arrange
        when(policyRepository.findByClientId(1L)).thenReturn(Arrays.asList());

        // Act
        var policies = policyService.listPoliciesByClientId(1L);

        // Assert
        assertTrue(policies.isEmpty());
    }

    @Test
    @DisplayName("Should delete policy successfully")
    void testDeletePolicy_Success() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));

        // Act
        policyService.deletePolicy(1L);

        // Assert
        verify(policyRepository, times(1)).delete(testPolicy);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent policy")
    void testDeletePolicy_NotFound() {
        // Arrange
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            policyService.deletePolicy(999L);
        });
        verify(policyRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should update policy successfully")
    void testUpdatePolicy_Success() {
        // Arrange
        when(policyRepository.findById(1L)).thenReturn(Optional.of(testPolicy));
        when(policyRepository.save(any(Policy.class))).thenReturn(testPolicy);

        testPolicy.setStatus("INACTIVE");
        testPolicy.setPremium(new BigDecimal("600000"));

        // Act
        Policy updatedPolicy = policyService.updatePolicy(1L, testPolicy);

        // Assert
        assertNotNull(updatedPolicy);
        assertEquals("INACTIVE", updatedPolicy.getStatus());
        verify(policyRepository, times(1)).save(any(Policy.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent policy")
    void testUpdatePolicy_NotFound() {
        // Arrange
        when(policyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            policyService.updatePolicy(999L, testPolicy);
        });
        verify(policyRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should list all policies successfully")
    void testListAllPolicies() {
        // Arrange
        Policy policy2 = Policy.builder()
                .id(2L)
                .policyNo("POL-002")
                .type(PolicyType.SALUD)
                .premium(new BigDecimal("850000"))
                .status("ACTIVE")
                .build();

        when(policyRepository.findAll()).thenReturn(Arrays.asList(testPolicy, policy2));

        // Act
        var policies = policyService.listAllPolicies();

        // Assert
        assertEquals(2, policies.size());
        verify(policyRepository, times(1)).findAll();
    }
}
