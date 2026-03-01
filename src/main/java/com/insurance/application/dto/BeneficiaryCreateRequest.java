package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear un beneficiario")
public class BeneficiaryCreateRequest {

    @NotNull(message = "El nombre completo es requerido")
    @NotBlank(message = "El nombre completo no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre completo del beneficiario", example = "María García López")
    private String fullName;

    @NotNull(message = "La relación es requerida")
    @NotBlank(message = "La relación no puede estar vacía")
    @Size(min = 2, max = 50, message = "La relación debe tener entre 2 y 50 caracteres")
    @Schema(description = "Relación con el asegurado", example = "Esposa")
    private String relationship;

    @NotNull(message = "El porcentaje es requerido")
    @DecimalMin(value = "0.01", message = "El porcentaje debe ser mayor a 0")
    @DecimalMax(value = "100", message = "El porcentaje no puede exceder 100")
    @Schema(description = "Porcentaje de participación (0-100)", example = "50")
    private Double percentage;

    @PastOrPresent(message = "La fecha de nacimiento debe ser en el pasado")
    @Schema(description = "Fecha de nacimiento", example = "1988-05-20")
    private LocalDate birthDate;
}
