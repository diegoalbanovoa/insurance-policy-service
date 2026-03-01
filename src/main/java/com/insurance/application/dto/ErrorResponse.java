package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta de error")
public class ErrorResponse {

    @Schema(description = "Timestamp del error", example = "2026-02-28T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Código HTTP del error", example = "400")
    private Integer status;

    @Schema(description = "Mensaje de error", example = "Validación fallida")
    private String message;

    @Schema(description = "Ruta del endpoint que causó el error", example = "/api/v1/clients")
    private String path;

    @Schema(description = "Detalles adicionales del error", example = "El cliente ya existe con ese documento")
    private String details;
}
