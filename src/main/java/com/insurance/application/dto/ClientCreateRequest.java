package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Datos para crear un cliente")
public class ClientCreateRequest {

    @NotNull(message = "El tipo de documento es requerido")
    @NotBlank(message = "El tipo de documento no puede estar vacío")
    @Size(min = 2, max = 10, message = "El tipo de documento debe tener entre 2 y 10 caracteres")
    @Schema(description = "Tipo de documento (CC, TI, CE, etc)", example = "CC")
    private String tipoDocumento;

    @NotNull(message = "El número de documento es requerido")
    @NotBlank(message = "El número de documento no puede estar vacío")
    @Pattern(regexp = "^[0-9]{7,20}$", message = "El número de documento debe contener solo dígitos y tener entre 7 y 20 caracteres")
    @Schema(description = "Número de documento único", example = "1234567890")
    private String numeroDocumento;

    @NotNull(message = "El nombre es requerido")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúñÁÉÍÓÚÑ\\s]+$", message = "El nombre solo puede contener letras")
    @Schema(description = "Nombre(s) del cliente", example = "Juan")
    private String nombres;

    @NotNull(message = "El apellido es requerido")
    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúñÁÉÍÓÚÑ\\s]+$", message = "El apellido solo puede contener letras")
    @Schema(description = "Apellido(s) del cliente", example = "Pérez García")
    private String apellidos;

    @NotNull(message = "El email es requerido")
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    @Schema(description = "Correo electrónico único del cliente", example = "juan@example.com")
    private String email;

    @NotNull(message = "El teléfono es requerido")
    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos y puede incluir +")
    @Schema(description = "Número de teléfono", example = "+573201234567")
    private String telefono;

    @NotNull(message = "La fecha de nacimiento es requerida")
    @PastOrPresent(message = "La fecha de nacimiento debe ser en el pasado o presente")
    @Schema(description = "Fecha de nacimiento", example = "1990-01-15")
    private LocalDate fechaNacimiento;
}
