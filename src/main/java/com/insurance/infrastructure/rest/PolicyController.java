package com.insurance.infrastructure.rest;

import com.insurance.application.dto.*;
import com.insurance.application.usecase.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de pólizas.
 * Expone endpoints para CRUD de pólizas y gestión de beneficiarios, vehículos y dependientes.
 */
@RestController
@RequestMapping("/api/v1/policies")
@RequiredArgsConstructor
@Tag(name = "Policies", description = "APIs para la gestión de pólizas")
public class PolicyController {

    private final PolicyService policyService;

    /**
     * Crea una nueva póliza para un cliente.
     *
     * @param request datos de la póliza a crear
     * @return ResponseEntity con los datos de la póliza creada (201 Created)
     */
    @PostMapping
    @Operation(summary = "Crear una nueva póliza",
               description = "Crea una nueva póliza. Valida cliente existe, " +
                           "fechas válidas y reglas de negocio (ej: máximo 1 póliza VIDA por cliente)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Póliza creada exitosamente",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = PolicyResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validación fallida"),
        @ApiResponse(responseCode = "409", description = "Regla de negocio violada")
    })
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody PolicyCreateRequest request) {
        PolicyResponse response = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene una póliza por su ID.
     *
     * @param id identificador de la póliza
     * @return ResponseEntity con los datos de la póliza
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener póliza por ID",
               description = "Recupera los datos de una póliza específica con beneficiarios, " +
                           "vehículos y dependientes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Póliza encontrada",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = PolicyResponse.class))),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    public ResponseEntity<PolicyResponse> getPolicyById(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long id) {
        PolicyResponse response = policyService.getPolicyById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todas las pólizas de un cliente.
     *
     * @param clientId identificador del cliente
     * @return ResponseEntity con la lista de pólizas del cliente
     */
    @GetMapping("/client/{clientId}")
    @Operation(summary = "Listar pólizas por cliente",
               description = "Obtiene todas las pólizas asociadas a un cliente específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pólizas obtenidas",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = PolicyResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<List<PolicyResponse>> getPoliciesByClientId(
            @Parameter(description = "ID del cliente", example = "1")
            @PathVariable Long clientId) {
        List<PolicyResponse> response = policyService.getPoliciesByClientId(clientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista pólizas de un cliente de un tipo específico.
     *
     * @param clientId identificador del cliente
     * @param policyType tipo de póliza (VIDA, VEHICULO, SALUD)
     * @return ResponseEntity con la lista de pólizas filtradas
     */
    @GetMapping("/client/{clientId}/type/{policyType}")
    @Operation(summary = "Listar pólizas por cliente y tipo",
               description = "Obtiene pólizas de un cliente de un tipo específico (VIDA, VEHICULO, SALUD)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Pólizas obtenidas",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = PolicyResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<List<PolicyResponse>> getPoliciesByClientAndType(
            @Parameter(description = "ID del cliente", example = "1")
            @PathVariable Long clientId,
            @Parameter(description = "Tipo de póliza", example = "VIDA")
            @PathVariable String policyType) {
        List<PolicyResponse> response = policyService.getPoliciesByClientAndType(clientId, policyType);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza el estado de una póliza.
     *
     * @param id identificador de la póliza
     * @param newStatus nuevo estado
     * @return ResponseEntity con los datos de la póliza actualizada
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de póliza",
               description = "Cambia el estado de una póliza")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = PolicyResponse.class))),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    public ResponseEntity<PolicyResponse> updatePolicyStatus(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado", example = "CANCELADA")
            @RequestParam String newStatus) {
        PolicyResponse response = policyService.updatePolicyStatus(id, newStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una póliza.
     *
     * @param id identificador de la póliza
     * @return ResponseEntity sin contenido (204 No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar póliza",
               description = "Elimina una póliza del sistema. " +
                           "Nota: Elimina en cascada beneficiarios, vehículos y dependientes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Póliza eliminada"),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    public ResponseEntity<Void> deletePolicy(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long id) {
        policyService.deletePolicy(id);
        return ResponseEntity.noContent().build();
    }

    // ========================
    // BENEFICIARIOS (PÓLIZAS VIDA)
    // ========================

    /**
     * Agrega un beneficiario a una póliza de Vida.
     *
     * @param policyId identificador de la póliza
     * @param request datos del beneficiario
     * @return ResponseEntity con los datos del beneficiario creado (201 Created)
     */
    @PostMapping("/{policyId}/beneficiaries")
    @Operation(summary = "Agregar beneficiario a póliza VIDA",
               description = "Agrega un beneficiario a una póliza de Vida. " +
                           "Máximo 2 beneficiarios por póliza.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Beneficiario agregado",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = BeneficiaryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validación fallida"),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada"),
        @ApiResponse(responseCode = "409", description = "Limite de beneficiarios alcanzado")
    })
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long policyId,
            @Valid @RequestBody BeneficiaryCreateRequest request) {
        BeneficiaryResponse response = policyService.addBeneficiary(policyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista los beneficiarios de una póliza de Vida.
     *
     * @param policyId identificador de la póliza
     * @return ResponseEntity con la lista de beneficiarios
     */
    @GetMapping("/{policyId}/beneficiaries")
    @Operation(summary = "Listar beneficiarios de póliza VIDA",
               description = "Obtiene la lista de beneficiarios de una póliza de Vida")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Beneficiarios obtenidos",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = BeneficiaryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    public ResponseEntity<List<BeneficiaryResponse>> getBeneficiariesByPolicyId(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long policyId) {
        List<BeneficiaryResponse> response = policyService.getBeneficiariesByPolicyId(policyId);
        return ResponseEntity.ok(response);
    }

    // ========================
    // VEHÍCULOS (PÓLIZAS VEHÍCULO)
    // ========================

    /**
     * Agrega un vehículo a una póliza de Vehículo.
     *
     * @param policyId identificador de la póliza
     * @param request datos del vehículo
     * @return ResponseEntity con los datos del vehículo creado (201 Created)
     */
    @PostMapping("/{policyId}/vehicles")
    @Operation(summary = "Agregar vehículo a póliza VEHICULO",
               description = "Agrega un vehículo a una póliza de Vehículo. " +
                           "La placa debe ser única.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Vehículo agregado",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validación fallida"),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada"),
        @ApiResponse(responseCode = "409", description = "Placa ya está registrada")
    })
    public ResponseEntity<VehicleResponse> addVehicle(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long policyId,
            @Valid @RequestBody VehicleCreateRequest request) {
        VehicleResponse response = policyService.addVehicle(policyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista los vehículos de una póliza de Vehículo.
     *
     * @param policyId identificador de la póliza
     * @return ResponseEntity con la lista de vehículos
     */
    @GetMapping("/{policyId}/vehicles")
    @Operation(summary = "Listar vehículos de póliza VEHICULO",
               description = "Obtiene la lista de vehículos de una póliza de Vehículo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vehículos obtenidos",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = VehicleResponse.class))),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    public ResponseEntity<List<VehicleResponse>> getVehiclesByPolicyId(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long policyId) {
        List<VehicleResponse> response = policyService.getVehiclesByPolicyId(policyId);
        return ResponseEntity.ok(response);
    }

    // ========================
    // DEPENDIENTES (PÓLIZAS SALUD)
    // ========================

    /**
     * Agrega un dependiente a una póliza de Salud.
     *
     * @param policyId identificador de la póliza
     * @param request datos del dependiente
     * @return ResponseEntity con los datos del dependiente creado (201 Created)
     */
    @PostMapping("/{policyId}/dependents")
    @Operation(summary = "Agregar dependiente a póliza SALUD",
               description = "Agrega un dependiente a una póliza de Salud")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Dependiente agregado",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = DependentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validación fallida"),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada"),
        @ApiResponse(responseCode = "409", description = "Póliza no es de tipo SALUD")
    })
    public ResponseEntity<DependentResponse> addDependent(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long policyId,
            @Valid @RequestBody DependentCreateRequest request) {
        DependentResponse response = policyService.addDependent(policyId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Lista los dependientes de una póliza de Salud.
     *
     * @param policyId identificador de la póliza
     * @return ResponseEntity con la lista de dependientes
     */
    @GetMapping("/{policyId}/dependents")
    @Operation(summary = "Listar dependientes de póliza SALUD",
               description = "Obtiene la lista de dependientes de una póliza de Salud")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Dependientes obtenidos",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = DependentResponse.class))),
        @ApiResponse(responseCode = "404", description = "Póliza no encontrada")
    })
    public ResponseEntity<List<DependentResponse>> getDependentsByPolicyId(
            @Parameter(description = "ID de la póliza", example = "1")
            @PathVariable Long policyId) {
        List<DependentResponse> response = policyService.getDependentsByPolicyId(policyId);
        return ResponseEntity.ok(response);
    }
}
