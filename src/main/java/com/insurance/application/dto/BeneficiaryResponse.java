package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un beneficiario")
public class BeneficiaryResponse {

    @Schema(description = "ID único del beneficiario", example = "1")
    private Long id;

    @Schema(description = "Nombre completo del beneficiario", example = "María García López")
    private String fullName;

    @Schema(description = "Relación con el asegurado", example = "Esposa")
    private String relationship;

    @Schema(description = "Porcentaje de participación", example = "50")
    private Double percentage;

    @Schema(description = "Fecha de nacimiento", example = "1988-05-20")
    private LocalDate birthDate;
}
