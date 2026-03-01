package com.insurance.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(
    title = "Crear Cliente",
    description = "Datos completos requeridos para registrar un nuevo cliente en el sistema. " +
                 "Todos los campos son obligatorios. El email y número de documento deben ser únicos.",
    example = "{\"tipoDocumento\":\"CC\",\"numeroDocumento\":\"1234567890\",\"nombres\":\"Juan\",\"apellidos\":\"Pérez García\"," +
             "\"email\":\"juan@insurance.com\",\"telefono\":\"+573201234567\",\"fechaNacimiento\":\"1990-01-15\"}"
)
public class ClientCreateRequest {

    @NotNull(message = "El tipo de documento es requerido")
    @NotBlank(message = "El tipo de documento no puede estar vacío")
    @Size(min = 2, max = 10, message = "El tipo de documento debe tener entre 2 y 10 caracteres")
    @Schema(
        title = "Tipo de documento",
        description = "Código que identifica el tipo de documento del cliente. Valores válidos: CC (Cédula de Ciudadanía), " +
                     "TI (Tarjeta de Identidad), CE (Cédula de Extranjería), PA (Pasaporte), etc. " +
                     "Este campo junto con el número de documento debe ser único en el sistema.",
        example = "CC",
        minLength = 2,
        maxLength = 10,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String tipoDocumento;

    @NotNull(message = "El número de documento es requerido")
    @NotBlank(message = "El número de documento no puede estar vacío")
    @Pattern(regexp = "^[0-9]{7,20}$", message = "El número de documento debe contener solo dígitos y tener entre 7 y 20 caracteres")
    @Schema(
        title = "Número de documento",
        description = "Número único que identifica al cliente. Debe contener solo dígitos (7-20 caracteres). " +
                     "Este campo debe ser único en el sistema - no puede haber dos clientes con el mismo documento independientemente del tipo. " +
                     "Se usa para búsquedas y validaciones de duplicados.",
        example = "1234567890",
        minLength = 7,
        maxLength = 20,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String numeroDocumento;

    @NotNull(message = "El nombre es requerido")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúñÁÉÍÓÚÑ\\s]+$", message = "El nombre solo puede contener letras")
    @Schema(
        title = "Nombre(s)",
        description = "Primer nombre o nombres del cliente. Solo permite letras (incluyendo acentos) y espacios. " +
                     "Se usa en reportes y comunicaciones con el cliente. Mínimo 2 caracteres.",
        example = "Juan",
        minLength = 2,
        maxLength = 50,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String nombres;

    @NotNull(message = "El apellido es requerido")
    @NotBlank(message = "El apellido no puede estar vacío")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúñÁÉÍÓÚÑ\\s]+$", message = "El apellido solo puede contener letras")
    @Schema(
        title = "Apellido(s)",
        description = "Uno o más apellidos del cliente. Solo permite letras (incluyendo acentos) y espacios. " +
                     "Puede contener múltiples apellidos separados por espacios. Se usa en reportes y documentos oficiales.",
        example = "Pérez García",
        minLength = 2,
        maxLength = 100,
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String apellidos;

    @NotNull(message = "El email es requerido")
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El email debe ser válido")
    @Schema(
        title = "Email",
        description = "Correo electrónico único del cliente. Se utiliza para: notificaciones, cambio de contraseña, " +
                     "comprobantes de pólizas y comunicaciones importantes. Debe ser válido y único - no puede estar asociado " +
                     "a otro cliente en el sistema. Se recomienda usar un email personal o corporativo activo.",
        example = "juan@insurance.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

    @NotNull(message = "El teléfono es requerido")
    @NotBlank(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[+]?[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos y puede incluir +")
    @Schema(
        title = "Teléfono",
        description = "Número de contacto del cliente. Permite formato internacional con prefijo '+' y entre 7-15 dígitos. " +
                     "Ejemplo: +573201234567 o 3201234567. Se usa para contactos urgentes y notificaciones por SMS.",
        example = "+573201234567",
        pattern = "^[+]?[0-9]{7,15}$",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String telefono;

    @NotNull(message = "La fecha de nacimiento es requerida")
    @PastOrPresent(message = "La fecha de nacimiento debe ser en el pasado o presente")
    @Schema(
        title = "Fecha de nacimiento",
        description = "Fecha de nacimiento del cliente en formato ISO (yyyy-MM-dd). Debe ser una fecha pasada o presente. " +
                     "Se utiliza para calcular edad, determinar capacidad legal y crear reportes demográficos. " +
                     "Ejemplo: 1990-01-15 para 15 de enero de 1990.",
        example = "1990-01-15",
        format = "date",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDate fechaNacimiento;
}
