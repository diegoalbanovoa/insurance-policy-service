package com.insurance.application.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Solicitud de autenticación para acceder a los servicios protegidos.
 * 
 * <h3>Descripción:</h3>
 * Contiene las credenciales (email y contraseña) necesarias para autenticar un usuario
 * y obtener tokens JWT válidos.
 * 
 * <h3>Flujo de autenticación:</h3>
 * <ol>
 *   <li>Enviar email y contraseña válidos</li>
 *   <li>Sistema valida credenciales contra la base de datos</li>
 *   <li>Si son válidas, retorna access_token y refresh_token</li>
 *   <li>Usar access_token en header Authorization para peticiones futuras</li>
 * </ol>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    title = "AuthenticationRequest",
    description = "Credenciales para autenticar un usuario y obtener JWT tokens",
    example = "{\"email\":\"usuario@insurance.com\",\"password\":\"SecurePass123!\"}"
)
public class AuthenticationRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    @Schema(
        title = "Email",
        description = "Correo electrónico registrado del usuario. Debe ser un email válido y único en el sistema.",
        example = "usuario@insurance.com",
        minLength = 5,
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 255, message = "La contraseña debe tener entre 6 y 255 caracteres")
    @Schema(
        title = "Contraseña",
        description = "Contraseña de acceso del usuario. Debe tener al menos 6 caracteres. " +
                     "Se recomienda usar mayúsculas, minúsculas, números y caracteres especiales para mayor seguridad.",
        example = "SecurePass123!",
        minLength = 6,
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;
}
