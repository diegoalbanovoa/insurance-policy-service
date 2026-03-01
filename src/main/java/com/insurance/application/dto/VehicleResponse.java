package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un vehículo asegurado")
public class VehicleResponse {

    @Schema(description = "ID único del vehículo", example = "1")
    private Long id;

    @Schema(description = "Placa del vehículo", example = "ABC-1234")
    private String plate;

    @Schema(description = "Marca del vehículo", example = "Toyota")
    private String brand;

    @Schema(description = "Modelo del vehículo", example = "Corolla")
    private String model;

    @Schema(description = "Año del vehículo", example = "2022")
    private Integer year;

    @Schema(description = "Tipo de vehículo", example = "Auto")
    private String vehicleType;
}
