package com.insurance.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud para renovar el token de acceso.
 * 
 * <h3>Descripción:</h3>
 * Se utiliza para obtener un nuevo access_token cuando el actual está próximo a expirar,
 * sin necesidad de reautenticar con email y contraseña.
 * 
 * <h3>Caso de uso:</h3>
 * <ol>
 *   <li>El access_token está próximo a expirar (menos de 1 hora)</li>
 *   <li>Usuario quiere mantener la sesión activa</li>
 *   <li>Enviar refresh_token a este endpoint</li>
 *   <li>Recibir nuevo access_token válido por 24 horas más</li>
 *   <li>Continuar operando sin interrupciones</li>
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    title = "RefreshTokenRequest",
    description = "Solicitud para renovar el access_token usando un refresh_token válido"
)
public class RefreshTokenRequest {

    @NotBlank(message = "El refresh token es requerido")
    @Schema(
        title = "Refresh token",
        description = "Token especial con vigencia de 7 días obtenido en login o registro. " +
                     "Se utiliza para obtener un nuevo access_token sin reautenticar. " +
                     "Mantener confidencial en el cliente.",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;
}
