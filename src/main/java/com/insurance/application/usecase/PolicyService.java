package com.insurance.application.usecase;

import com.insurance.application.dto.*;
import com.insurance.domain.model.*;
import com.insurance.domain.repository.*;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import com.insurance.shared.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final ClientRepository clientRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final VehicleRepository vehicleRepository;
    private final DependentRepository dependentRepository;

    private static final DateTimeFormatter POLICY_NUMBER_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Crea una nueva póliza para un cliente.
     * Incluye validaciones de:
     * - Cliente existe
     * - Fechas válidas (startDate < endDate)
     * - Reglas de negocio (solo 1 póliza VIDA por cliente)
     *
     * @param request datos de la póliza a crear
     * @return DTO de la póliza creada
     * @throws ResourceNotFoundException si el cliente no existe
     * @throws ValidationException si las fechas son inválidas
     * @throws BusinessRuleException si se viola una regla de negocio
     */
    @SuppressWarnings("null")
    public PolicyResponse createPolicy(PolicyCreateRequest request) {
        // Validar que el cliente existe
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClientId()));

        // Validar fechas
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new ValidationException("La fecha de inicio debe ser anterior a la fecha de fin");
        }

        if (request.getStartDate().equals(request.getEndDate())) {
            throw new ValidationException("La fecha de inicio y fin no pueden ser iguales");
        }

        // Validar reglas de negocio según tipo de póliza
        PolicyType policyType = PolicyType.valueOf(request.getPolicyType());
        
        if (policyType == PolicyType.VIDA) {
            long existingVidaPolicies = policyRepository.countByClientIdAndPolicyType(
                request.getClientId(), 
                PolicyType.VIDA
            );
            if (existingVidaPolicies > 0) {
                throw new BusinessRuleException("El cliente ya tiene una póliza de VIDA activa");
            }
        }

        // Generar número de póliza único
        String policyNumber = generatePolicyNumber(policyType);

        // Crear y guardar la póliza
        Policy policy = Policy.builder()
                .policyNumber(policyNumber)
                .policyType(policyType)
                .client(client)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .premiumAmount(request.getPremiumAmount())
                .status(request.getStatus())
                .build();

        Policy savedPolicy = policyRepository.save(policy);
        return mapToResponse(savedPolicy);
    }

    /**
     * Obtiene una póliza por su ID.
     *
     * @param id identificador de la póliza
     * @return DTO de la póliza
     * @throws ResourceNotFoundException si la póliza no existe
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public PolicyResponse getPolicyById(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza", id));
        return mapToResponse(policy);
    }

    /**
     * Lista todas las pólizas de un cliente.
     *
     * @param clientId identificador del cliente
     * @return lista de DTOs de pólizas
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<PolicyResponse> getPoliciesByClientId(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", clientId);
        }
        return policyRepository.findByClientId(clientId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista pólizas de un cliente de un tipo específico.
     *
     * @param clientId identificador del cliente
     * @param policyType tipo de póliza (VIDA, VEHICULO, SALUD)
     * @return lista de DTOs de pólizas del tipo especificado
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<PolicyResponse> getPoliciesByClientAndType(Long clientId, String policyType) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", clientId);
        }
        PolicyType type = PolicyType.valueOf(policyType);
        return policyRepository.findByClientIdAndPolicyType(clientId, type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza el estado de una póliza.
     *
     * @param id identificador de la póliza
     * @param newStatus nuevo estado
     * @return DTO de la póliza actualizada
     * @throws ResourceNotFoundException si la póliza no existe
     */
    @SuppressWarnings("null")
    public PolicyResponse updatePolicyStatus(Long id, String newStatus) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza", id));
        
        policy.setStatus(newStatus);
        Policy updatedPolicy = policyRepository.save(policy);
        return mapToResponse(updatedPolicy);
    }

    /**
     * Elimina una póliza por su ID.
     *
     * @param id identificador de la póliza
     * @throws ResourceNotFoundException si la póliza no existe
     */
    @SuppressWarnings("null")
    public void deletePolicy(Long id) {
        if (!policyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Póliza", id);
        }
        policyRepository.deleteById(id);
    }

    /**
     * Agrega un beneficiario a una póliza de Vida.
     *
     * @param policyId identificador de la póliza
     * @param request datos del beneficiario
     * @return DTO del beneficiario creado
     * @throws ResourceNotFoundException si la póliza no existe
     * @throws BusinessRuleException si la póliza no es de VIDA o ya tiene 2 beneficiarios
     */
    @SuppressWarnings("null")
    public BeneficiaryResponse addBeneficiary(Long policyId, BeneficiaryCreateRequest request) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza", policyId));

        if (policy.getPolicyType() != PolicyType.VIDA) {
            throw new BusinessRuleException("Solo se pueden agregar beneficiarios a pólizas de VIDA");
        }

        long beneficiaryCount = beneficiaryRepository.countByPolicyId(policyId);
        if (beneficiaryCount >= 2) {
            throw new BusinessRuleException("Una póliza de VIDA puede tener máximo 2 beneficiarios");
        }

        Beneficiary beneficiary = Beneficiary.builder()
                .policy(policy)
                .fullName(request.getFullName())
                .relationship(request.getRelationship())
                .percentage(request.getPercentage())
                .birthDate(request.getBirthDate())
                .build();

        Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);
        return mapBeneficiaryToResponse(savedBeneficiary);
    }

    /**
     * Lista los beneficiarios de una póliza de Vida.
     *
     * @param policyId identificador de la póliza
     * @return lista de DTOs de beneficiarios
     * @throws ResourceNotFoundException si la póliza no existe
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<BeneficiaryResponse> getBeneficiariesByPolicyId(Long policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new ResourceNotFoundException("Póliza", policyId);
        }
        return beneficiaryRepository.findByPolicyId(policyId).stream()
                .map(this::mapBeneficiaryToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Agrega un vehículo a una póliza de Vehículo.
     *
     * @param policyId identificador de la póliza
     * @param request datos del vehículo
     * @return DTO del vehículo creado
     * @throws ResourceNotFoundException si la póliza no existe
     * @throws BusinessRuleException si la póliza no es de VEHICULO
     * @throws BusinessRuleException si la placa ya está registrada
     */
    @SuppressWarnings("null")
    public VehicleResponse addVehicle(Long policyId, VehicleCreateRequest request) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza", policyId));

        if (policy.getPolicyType() != PolicyType.VEHICULO) {
            throw new BusinessRuleException("Solo se pueden agregar vehículos a pólizas de VEHICULO");
        }

        // Validar que la placa no está registrada en otro vehículo
        vehicleRepository.findByPlate(request.getPlate()).ifPresent(v -> {
            throw new BusinessRuleException("La placa " + request.getPlate() + " ya está registrada");
        });

        Vehicle vehicle = Vehicle.builder()
                .policy(policy)
                .plate(request.getPlate())
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .vehicleType(request.getVehicleType())
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return mapVehicleToResponse(savedVehicle);
    }

    /**
     * Lista los vehículos de una póliza de Vehículo.
     *
     * @param policyId identificador de la póliza
     * @return lista de DTOs de vehículos
     * @throws ResourceNotFoundException si la póliza no existe
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<VehicleResponse> getVehiclesByPolicyId(Long policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new ResourceNotFoundException("Póliza", policyId);
        }
        return vehicleRepository.findByPolicyId(policyId).stream()
                .map(this::mapVehicleToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Agrega un dependiente a una póliza de Salud.
     *
     * @param policyId identificador de la póliza
     * @param request datos del dependiente
     * @return DTO del dependiente creado
     * @throws ResourceNotFoundException si la póliza no existe
     * @throws BusinessRuleException si la póliza no es de SALUD
     */
    @SuppressWarnings("null")
    public DependentResponse addDependent(Long policyId, DependentCreateRequest request) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Póliza", policyId));

        if (policy.getPolicyType() != PolicyType.SALUD) {
            throw new BusinessRuleException("Solo se pueden agregar dependientes a pólizas de SALUD");
        }

        Dependent dependent = Dependent.builder()
                .policy(policy)
                .fullName(request.getFullName())
                .relationship(request.getRelationship())
                .birthDate(request.getBirthDate())
                .dependentType(request.getDependentType())
                .build();

        Dependent savedDependent = dependentRepository.save(dependent);
        return mapDependentToResponse(savedDependent);
    }

    /**
     * Lista los dependientes de una póliza de Salud.
     *
     * @param policyId identificador de la póliza
     * @return lista de DTOs de dependientes
     * @throws ResourceNotFoundException si la póliza no existe
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public List<DependentResponse> getDependentsByPolicyId(Long policyId) {
        if (!policyRepository.existsById(policyId)) {
            throw new ResourceNotFoundException("Póliza", policyId);
        }
        return dependentRepository.findByPolicyId(policyId).stream()
                .map(this::mapDependentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Genera un número de póliza único basado en la fecha actual y tipo de póliza.
     *
     * @param policyType tipo de póliza
     * @return número de póliza único (ej: POL-20260228-VIDA-00001)
     */
    private String generatePolicyNumber(PolicyType policyType) {
        String datePrefix = LocalDate.now().format(POLICY_NUMBER_FORMAT);
        String typePrefix = policyType.name();
        long policyCount = policyRepository.count() + 1;
        return String.format("POL-%s-%s-%05d", datePrefix, typePrefix, policyCount);
    }

    /**
     * Mapea una entidad Policy a su DTO correspondiente.
     */
    private PolicyResponse mapToResponse(Policy policy) {
        return PolicyResponse.builder()
                .id(policy.getId())
                .policyNumber(policy.getPolicyNumber())
                .policyType(policy.getPolicyType().name())
                .clientId(policy.getClient().getId())
                .clientName(policy.getClient().getFullName())
                .startDate(policy.getStartDate())
                .endDate(policy.getEndDate())
                .premiumAmount(policy.getPremiumAmount())
                .status(policy.getStatus())
                .beneficiaries(policy.getBeneficiaries().stream()
                        .map(this::mapBeneficiaryToResponse)
                        .collect(Collectors.toList()))
                .vehicles(policy.getVehicles().stream()
                        .map(this::mapVehicleToResponse)
                        .collect(Collectors.toList()))
                .dependents(policy.getDependents().stream()
                        .map(this::mapDependentToResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Mapea una entidad Beneficiary a su DTO correspondiente.
     */
    private BeneficiaryResponse mapBeneficiaryToResponse(Beneficiary beneficiary) {
        return BeneficiaryResponse.builder()
                .id(beneficiary.getId())
                .fullName(beneficiary.getFullName())
                .relationship(beneficiary.getRelationship())
                .percentage(beneficiary.getPercentage())
                .birthDate(beneficiary.getBirthDate())
                .build();
    }

    /**
     * Mapea una entidad Vehicle a su DTO correspondiente.
     */
    private VehicleResponse mapVehicleToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .plate(vehicle.getPlate())
                .brand(vehicle.getBrand())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .vehicleType(vehicle.getVehicleType())
                .build();
    }

    /**
     * Mapea una entidad Dependent a su DTO correspondiente.
     */
    private DependentResponse mapDependentToResponse(Dependent dependent) {
        return DependentResponse.builder()
                .id(dependent.getId())
                .fullName(dependent.getFullName())
                .relationship(dependent.getRelationship())
                .birthDate(dependent.getBirthDate())
                .dependentType(dependent.getDependentType())
                .build();
    }
}
