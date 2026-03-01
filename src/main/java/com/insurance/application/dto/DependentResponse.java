package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un dependiente")
public class DependentResponse {

    @Schema(description = "ID único del dependiente", example = "1")
    private Long id;

    @Schema(description = "Nombre completo del dependiente", example = "Carlos García López")
    private String fullName;

    @Schema(description = "Relación con el asegurado", example = "Hijo")
    private String relationship;

    @Schema(description = "Fecha de nacimiento", example = "2010-08-15")
    private LocalDate birthDate;

    @Schema(description = "Tipo de dependiente", example = "Hijo")
    private String dependentType;
}
