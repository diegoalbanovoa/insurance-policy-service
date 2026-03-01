package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Schema(description = "Datos completos de una póliza")
public class PolicyResponse {

    @Schema(description = "ID único de la póliza", example = "1")
    private Long id;

    @Schema(description = "Número de póliza", example = "POL-2026-00001")
    private String policyNumber;

    @Schema(description = "Tipo de póliza", example = "VIDA")
    private String policyType;

    @Schema(description = "ID del cliente propietario", example = "1")
    private Long clientId;

    @Schema(description = "Nombre completo del cliente propietario", example = "Juan Pérez García")
    private String clientName;

    @Schema(description = "Fecha de inicio", example = "2026-03-01")
    private LocalDate startDate;

    @Schema(description = "Fecha de vencimiento", example = "2027-03-01")
    private LocalDate endDate;

    @Schema(description = "Monto de la prima", example = "500000.00")
    private Double premiumAmount;

    @Schema(description = "Estado actual", example = "ACTIVA")
    private String status;

    @Schema(description = "Beneficiarios asociados (para pólizas de VIDA)")
    @Builder.Default
    private List<BeneficiaryResponse> beneficiaries = new ArrayList<>();

    @Schema(description = "Vehículos asegurados (para pólizas de VEHICULO)")
    @Builder.Default
    private List<VehicleResponse> vehicles = new ArrayList<>();

    @Schema(description = "Dependientes cubiertos (para pólizas de SALUD)")
    @Builder.Default
    private List<DependentResponse> dependents = new ArrayList<>();
}
