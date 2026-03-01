package com.insurance.application.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta exitosa de autenticación con tokens JWT.
 * 
 * <h3>Descripción:</h3>
 * Retornada tras una autenticación o registro exitoso. Contiene los tokens necesarios
 * para acceder a endpoints protegidos.
 * 
 * <h3>Instrucciones de uso:</h3>
 * <ol>
 *   <li>Guardar access_token (válido 24 horas)</li>
 *   <li>Incluir en header Authorization: "Bearer {access_token}"</li>
 *   <li>Usar refresh_token para obtener nuevo access_token antes de expiración</li>
 *   <li>Al expirar, usar refresh_token para renovar sin reautenticar</li>
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    title = "AuthenticationResponse",
    description = "Respuesta exitosa con tokens JWT y datos del usuario autenticado"
)
public class AuthenticationResponse {

    @JsonProperty("access_token")
    @Schema(
        title = "Token de acceso",
        description = "JWT token válido por 24 horas. Usar en header Authorization como 'Bearer {token}' " +
                     "para acceder a endpoints protegidos. Se expira automáticamente tras 24 horas de inactividad.",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String accessToken;

    @JsonProperty("refresh_token")
    @Schema(
        title = "Token de refresco",
        description = "Token especial válido por 7 días. Usar en el endpoint /auth/refresh para obtener " +
                     "un nuevo access_token sin necesidad de reautenticar. Mantener seguro en el cliente.",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String refreshToken;

    @JsonProperty("token_type")
    @Schema(
        title = "Tipo de token",
        description = "Tipo de autenticación utilizado. Siempre es 'Bearer' para JWT.",
        example = "Bearer",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String tokenType;

    @JsonProperty("expires_in")
    @Schema(
        title = "Tiempo de expiración",
        description = "Tiempo en milisegundos hasta que el access_token expire. Por defecto 86400000ms (24 horas).",
        example = "86400000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private long expiresIn;

    @Schema(
        title = "Nombre de usuario",
        description = "Nombre completo del usuario autenticado.",
        example = "Juan Pérez García"
    )
    private String username;

    @Schema(
        title = "Email",
        description = "Correo electrónico del usuario autenticado.",
        example = "usuario@insurance.com"
    )
    private String email;
}
