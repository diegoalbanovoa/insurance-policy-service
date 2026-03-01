package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear un vehículo asegurado")
public class VehicleCreateRequest {

    @NotNull(message = "La placa es requerida")
    @NotBlank(message = "La placa no puede estar vacía")
    @Pattern(regexp = "^[A-Z0-9-]{5,20}$", message = "La placa debe tener un formato válido")
    @Schema(description = "Placa del vehículo", example = "ABC-1234")
    private String plate;

    @NotNull(message = "La marca es requerida")
    @NotBlank(message = "La marca no puede estar vacía")
    @Size(min = 2, max = 100, message = "La marca debe tener entre 2 y 100 caracteres")
    @Schema(description = "Marca del vehículo", example = "Toyota")
    private String brand;

    @NotNull(message = "El modelo es requerido")
    @NotBlank(message = "El modelo no puede estar vacío")
    @Size(min = 2, max = 100, message = "El modelo debe tener entre 2 y 100 caracteres")
    @Schema(description = "Modelo del vehículo", example = "Corolla")
    private String model;

    @NotNull(message = "El año es requerido")
    @Min(value = 1900, message = "El año debe ser válido")
    @Max(value = 2100, message = "El año debe ser válido")
    @Schema(description = "Año del vehículo", example = "2022")
    private Integer year;

    @NotNull(message = "El tipo de vehículo es requerido")
    @NotBlank(message = "El tipo de vehículo no puede estar vacío")
    @Size(min = 2, max = 50, message = "El tipo debe tener entre 2 y 50 caracteres")
    @Schema(description = "Tipo de vehículo (Auto, Moto, Camión, etc)", example = "Auto")
    private String vehicleType;
}
