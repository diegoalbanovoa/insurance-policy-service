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
 * Solicitud de registro para crear una nueva cuenta de usuario.
 * 
 * <h3>Descripción:</h3>
 * Contiene todos los datos necesarios para registrar un nuevo usuario en el sistema.
 * Los usuarios registrados tendrán acceso a los servicios protegidos con autenticación JWT.
 * 
 * <h3>Validaciones:</h3>
 * <ul>
 *   <li>Email debe ser único y válido</li>
 *   <li>Contraseña mínimo 8 caracteres (se recomienda incluir mayúsculas, minúsculas, números)</li>
 *   <li>Requerimiento: confirmPassword debe coincidir exactamente con password</li>
 *   <li>Nombre completo: 2-100 caracteres alfanuméricos</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    title = "RegistrationRequest",
    description = "Datos para crear una nueva cuenta de usuario y obtener credenciales de acceso"
)
public class RegistrationRequest {

    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    @Schema(
        title = "Email",
        description = "Correo electrónico único para la cuenta. Será utilizado como identificador y para notificaciones. " +
                     "No puede estar asociado a otra cuenta en el sistema.",
        example = "nuevo.usuario@insurance.com",
        minLength = 5,
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, max = 255, message = "La contraseña debe tener entre 8 y 255 caracteres")
    @Schema(
        title = "Contraseña",
        description = "Contraseña de acceso - Mínimo 8 caracteres. Se recomienda: mayúsculas, minúsculas, números y caracteres especiales. " +
                     "Se almacena encriptada con BCrypt en la base de datos.",
        example = "MiContraseña123!@",
        minLength = 8,
        maxLength = 255,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    @NotBlank(message = "La confirmación de contraseña es requerida")
    @Schema(
        title = "Confirmar contraseña",
        description = "Debe ser idéntico al campo password. Se usa para prevenir errores de digitación. " +
                     "El sistema valida que ambos campos coincidan exactamente.",
        example = "MiContraseña123!@",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String confirmPassword;

    @NotBlank(message = "El nombre completo es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(
        title = "Nombre completo",
        description = "Nombre y apellido(s) completo del usuario. Se utiliza para identificación en reportes y notificaciones. " +
                     "Puede contener espacios y caracteres acentuados.",
        example = "Juan Pérez García",
        minLength = 2,
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String fullName;
}
