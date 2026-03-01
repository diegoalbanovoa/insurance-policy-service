package com.insurance.infrastructure.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para el estado de salud de la aplicación.
 */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "APIs para verificar el estado de la aplicación")
public class HealthController {

    /**
     * Verifica el estado de salud de la aplicación.
     *
     * @return ResponseEntity con el estado de salud
     */
    @GetMapping
    @Operation(summary = "Health Check",
               description = "Verifica que la aplicación está corriendo correctamente")
    @ApiResponse(responseCode = "200", description = "Aplicación funcionando correctamente")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Insurance Policy Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
