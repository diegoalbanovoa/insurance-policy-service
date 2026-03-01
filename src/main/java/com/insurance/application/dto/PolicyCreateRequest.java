package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear una póliza")
public class PolicyCreateRequest {

    @NotNull(message = "El tipo de póliza es requerido")
    @NotBlank(message = "El tipo de póliza no puede estar vacío")
    @Pattern(regexp = "^(VIDA|VEHICULO|SALUD)$", 
             message = "El tipo de póliza debe ser: VIDA, VEHICULO o SALUD")
    @Schema(description = "Tipo de póliza (VIDA, VEHICULO, SALUD)", example = "VIDA")
    private String policyType;

    @NotNull(message = "El ID del cliente es requerido")
    @Positive(message = "El ID del cliente debe ser un número positivo")
    @Schema(description = "ID del cliente propietario", example = "1")
    private Long clientId;

    @NotNull(message = "La fecha de inicio es requerida")
    @FutureOrPresent(message = "La fecha de inicio debe ser hoy o en el futuro")
    @Schema(description = "Fecha de inicio de vigencia", example = "2026-03-01")
    private LocalDate startDate;

    @NotNull(message = "La fecha de fin es requerida")
    @Future(message = "La fecha de fin debe ser en el futuro")
    @Schema(description = "Fecha de fin de vigencia", example = "2027-03-01")
    private LocalDate endDate;

    @NotNull(message = "El monto de la prima es requerido")
    @DecimalMin(value = "0.01", message = "El monto de la prima debe ser mayor a 0")
    @DecimalMax(value = "999999999.99", message = "El monto de la prima no puede exceder el límite")
    @Digits(integer = 10, fraction = 2, message = "El monto debe tener máximo 10 dígitos enteros y 2 decimales")
    @Schema(description = "Monto de la prima", example = "500000.00")
    private Double premiumAmount;

    @NotBlank(message = "El estado no puede estar vacío")
    @Pattern(regexp = "^(ACTIVA|CANCELADA|SUSPENDIDA|VENCIDA)$", 
             message = "El estado debe ser: ACTIVA, CANCELADA, SUSPENDIDA o VENCIDA")
    @Schema(description = "Estado inicial (por defecto ACTIVA)", example = "ACTIVA")
    @Builder.Default
    private String status = "ACTIVA";
}
