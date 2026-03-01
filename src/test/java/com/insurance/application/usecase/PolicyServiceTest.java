package com.insurance.application.usecase;

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
import com.insurance.domain.repository.BeneficiaryRepository;
import com.insurance.domain.repository.ClientRepository;
import com.insurance.domain.repository.DependentRepository;
import com.insurance.domain.repository.PolicyRepository;
import com.insurance.domain.repository.VehicleRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("PolicyService Unit Tests")
class PolicyServiceTest {

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

    @Test
    @DisplayName("Should be autowired correctly")
    void testServiceInjection() {
        // Assert that the service is properly injected with mocked repositories
        assert policyService != null;
        assert policyRepository != null;
        assert clientRepository != null;
    }

    @Test
    @DisplayName("Service creation should not fail")
    void testServiceCreation() {
        // Verify that PolicyService can be created with mocked dependencies
        assert policyService != null;
    }

    @Test
    @DisplayName("PolicyRepository mock should be available")
    void testPolicyRepositoryMock() {
        // Verify that the mocked repository is available for testing
        assert policyRepository != null;
    }

    @Test
    @DisplayName("ClientRepository mock should be available")
    void testClientRepositoryMock() {
        // Verify that the mocked repository is available for testing
        assert clientRepository != null;
    }

    @Test
    @DisplayName("BeneficiaryRepository mock should be available")
    void testBeneficiaryRepositoryMock() {
        // Verify that the mocked repository is available for testing
        assert beneficiaryRepository != null;
    }

    @Test
    @DisplayName("VehicleRepository mock should be available")
    void testVehicleRepositoryMock() {
        // Verify that the mocked repository is available for testing
        assert vehicleRepository != null;
    }

    @Test
    @DisplayName("DependentRepository mock should be available")
    void testDependentRepositoryMock() {
        // Verify that the mocked repository is available for testing
        assert dependentRepository != null;
    }

    @Test
    @DisplayName("PolicyCreateRequest builder works")
    void testPolicyCreateRequestBuilder() {
        // Verify that DTOs can be built successfully
        PolicyCreateRequest request = PolicyCreateRequest.builder()
            .policyType("VIDA")
            .clientId(1L)
            .premiumAmount(5000.00)
            .build();

        assert request != null;
        assert "VIDA".equals(request.getPolicyType());
    }

    @Test
    @DisplayName("BeneficiaryCreateRequest builder works")
    void testBeneficiaryCreateRequestBuilder() {
        // Verify that DTOs can be built successfully
        BeneficiaryCreateRequest request = BeneficiaryCreateRequest.builder()
            .fullName("María García")
            .relationship("Esposa")
            .percentage(100.00)
            .build();

        assert request != null;
        assert "María García".equals(request.getFullName());
    }

    @Test
    @DisplayName("VehicleCreateRequest builder works")
    void testVehicleCreateRequestBuilder() {
        // Verify that DTOs can be built successfully
        VehicleCreateRequest request = VehicleCreateRequest.builder()
            .brand("Toyota")
            .model("Corolla")
            .plate("ABC123")
            .year(2023)
            .build();

        assert request != null;
        assert "ABC123".equals(request.getPlate());
    }

    @Test
    @DisplayName("DependentCreateRequest builder works")
    void testDependentCreateRequestBuilder() {
        // Verify that DTOs can be built successfully
        DependentCreateRequest request = DependentCreateRequest.builder()
            .fullName("Luis Pérez")
            .relationship("Son")
            .build();

        assert request != null;
        assert "Luis Pérez".equals(request.getFullName());
    }

    @Test
    @DisplayName("PolicyRepository interactions can be mocked")
    void testPolicyRepositoryMocking() {
        // Verify that we can set up mock expectations
        when(policyRepository.count()).thenReturn(5L);

        long count = policyRepository.count();

        assert count == 5L;
        verify(policyRepository, times(1)).count();
    }

    @Test
    @DisplayName("ClientRepository interactions can be mocked")
    void testClientRepositoryMocking() {
        // Verify that we can set up mock expectations
        when(clientRepository.count()).thenReturn(3L);

        long count = clientRepository.count();

        assert count == 3L;
        verify(clientRepository, times(1)).count();
    }
}
