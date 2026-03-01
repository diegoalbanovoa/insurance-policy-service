package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear un dependiente")
public class DependentCreateRequest {

    @NotNull(message = "El nombre completo es requerido")
    @NotBlank(message = "El nombre completo no puede estar vacío")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre completo del dependiente", example = "Carlos García López")
    private String fullName;

    @NotNull(message = "La relación es requerida")
    @NotBlank(message = "La relación no puede estar vacía")
    @Size(min = 2, max = 50, message = "La relación debe tener entre 2 y 50 caracteres")
    @Schema(description = "Relación con el asegurado", example = "Hijo")
    private String relationship;

    @NotNull(message = "La fecha de nacimiento es requerida")
    @PastOrPresent(message = "La fecha de nacimiento debe ser en el pasado")
    @Schema(description = "Fecha de nacimiento", example = "2010-08-15")
    private LocalDate birthDate;

    @NotNull(message = "El tipo de dependiente es requerido")
    @NotBlank(message = "El tipo de dependiente no puede estar vacío")
    @Size(min = 2, max = 50, message = "El tipo debe tener entre 2 y 50 caracteres")
    @Schema(description = "Tipo de dependiente", example = "Hijo")
    private String dependentType;
}
