package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos de un cliente")
public class ClientResponse {

    @Schema(description = "ID único del cliente", example = "1")
    private Long id;

    @Schema(description = "Tipo de documento", example = "CC")
    private String tipoDocumento;

    @Schema(description = "Número de documento", example = "1234567890")
    private String numeroDocumento;

    @Schema(description = "Nombre(s) del cliente", example = "Juan")
    private String nombres;

    @Schema(description = "Apellido(s) del cliente", example = "Pérez García")
    private String apellidos;

    @Schema(description = "Nombre completo", example = "Juan Pérez García")
    private String fullName;

    @Schema(description = "Correo electrónico", example = "juan@example.com")
    private String email;

    @Schema(description = "Teléfono", example = "+573201234567")
    private String telefono;

    @Schema(description = "Fecha de nacimiento", example = "1990-01-15")
    private LocalDate fechaNacimiento;
}
