package com.insurance.presentation.controller;

import com.insurance.application.dto.auth.AuthenticationRequest;
import com.insurance.application.dto.auth.AuthenticationResponse;
import com.insurance.application.dto.auth.RefreshTokenRequest;
import com.insurance.application.dto.auth.RegistrationRequest;
import com.insurance.application.service.AuthenticationService;
import com.insurance.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(
    name = "Authentication",
    description = "APIs de autenticación y gestión de sesiones con JWT. " +
                 "Todos los endpoints devuelven tokens que deben incluirse en el header Authorization de futuras peticiones."
)
public class AuthenticationController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationService authenticationService;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * ENDPOINT: Login/Autenticación
     * 
     * <h3>Descripción:</h3>
     * Autentica un usuario existente usando email y contraseña. Si las credenciales son válidas,
     * devuelve un access_token JWT (válido 24 horas) y un refresh_token (válido 7 días).
     * 
     * <h3>Flujo de uso:</h3>
     * <ol>
     *   <li>Enviar POST con email y contraseña válidos</li>
     *   <li>Servidor valida credenciales contra base de datos</li>
     *   <li>Genera JWT tokens si son válidas</li>
     *   <li>Cliente guarda access_token en memoria/localStorage</li>
     *   <li>Cliente incluye access_token en header: Authorization: Bearer {token}</li>
     * </ol>
     * 
     * <h3>Manejo de errores:</h3>
     * <ul>
     *   <li>400: JSON malformado o email vacío</li>\n     *   <li>401: Email no existe o contraseña incorrecta</li>
     *   <li>500: Error interno del servidor</li>
     * </ul>
     * \n     * @param request Credenciales (email y contraseña)
     * @return AuthenticationResponse con access_token, refresh_token y datos del usuario
     */
    @PostMapping("/login")
    @Operation(
        summary = "Autenticar usuario con credenciales",
        description = "Verifica email y contraseña, genera tokens JWT si son válidas. " +
                     "El access_token es válido 24 horas, el refresh_token 7 días.",
        tags = {"Autenticación"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Autenticación exitosa. Devuelve tokens y datos del usuario.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthenticationResponse.class),
                examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                    value = "{\"access_token\":\"eyJhbGc...\",\"refresh_token\":\"eyJhbGc...\",\"token_type\":\"Bearer\"," +
                           "\"expires_in\":86400,\"username\":\"Juan Pérez\",\"email\":\"juan@insurance.com\"}"
                )
            )
        ),
        @ApiResponse(responseCode = "400", description = "Solicitud inválida o datos incompletos"),
        @ApiResponse(responseCode = "401", description = "Email no registrado o contraseña incorrecta"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        try {
            log.info("Intento de login para usuario: {}", request.getEmail());
            AuthenticationResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("Fallo en autenticación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error durante la autenticación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ENDPOINT: Registro de nuevo usuario
     * 
     * <h3>Descripción:</h3>
     * Crea una nueva cuenta de usuario en el sistema. Valida que el email sea único,
     * que las contraseñas coincidan y cumplan requisitos de seguridad.
     * Al completarse exitosamente, devuelve tokens JWT ya listos para usar.
     * 
     * <h3>Validaciones aplicadas:</h3>
     * <ul>
     *   <li>Email debe ser formato válido y único (no puede estar registrado)</li>
     *   <li>Contraseña mínimo 8 caracteres (se recomienda: mayús, minús, números, especiales)</li>
     *   <li>confirmPassword debe coincidir exactamente con password</li>
     *   <li>Nombre completo: 2-100 caracteres alfabéticos</li>
     * </ul>
     * 
     * <h3>Seguridad:</h3>
     * Las contraseñas se encriptan con BCrypt antes de almacenarlas. Nunca se devuelven las contraseñas.
     * 
     * <h3>Respuesta de éxito:</h3>
     * Retorna 201 Created con los tokens JWT y datos del usuario registrado.
     * El usuario puede usar estos tokens inmediatamente para acceder a endpoints protegidos.
     * 
     * @param request Datos de registro: email, password, confirmPassword, fullName
     * @return Mensaje de éxito y AuthenticationResponse con tokens
     */
    @PostMapping("/register")
    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea una nueva cuenta de usuario validando email único y requisitos de contraseña. " +
                     "Retorna tokens JWT listos para usar.",
        tags = {"Autenticación"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Usuario registrado exitosamente. Devuelve tokens JWT y datos del usuario.",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(responseCode = "400", description = "Email duplicado, contraseñas no coinciden o datos inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegistrationRequest request) {
        try {
            log.info("Intento de registro para usuario: {}", request.getEmail());
            AuthenticationResponse response = authenticationService.register(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    Map.of(
                            "message", "Usuario registrado exitosamente. Puede iniciar sesión inmediatamente.",
                            "data", response
                    )
            );
        } catch (IllegalArgumentException e) {
            log.warn("Error en registro: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        } catch (Exception e) {
            log.error("Error durante el registro: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Error interno del servidor")
            );
        }
    }

    /**
     * ENDPOINT: Renovar token de acceso
     * 
     * <h3>Descripción:</h3>
     * Genera un nuevo access_token sin requerer credenciales. Usa el refresh_token
     * obtenido en login/registro (válido 7 días). Permite mantener sesiones activas
     * sin solicitar contraseña nuevamente.
     * 
     * <h3>Cuándo usar:</h3>
     * <ul>
     *   <li>El access_token está próximo a expirar (menos de 1 hora)</li>
     *   <li>Usuario quiere mantener sesión abierta mientras usa la app</li>
     *   <li>En aplicaciones SPA que requieren inactividad prolongada</li>
     * </ul>
     * 
     * <h3>Proceso:</h3>
     * <ol>
     *   <li>Guardar refresh_token del login/registro</li>
     *   <li>Cuando access_token vence (después 24 horas)</li>
     *   <li>Enviar POST con refresh_token a este endpoint</li>
     *   <li>Recibir nuevo access_token válido por 24 horas más</li>
     *   <li>refresh_token sigue siendo el mismo (válido hasta su expiración a los 7 días)</li>
     * </ol>
     * 
     * <h3>Errores esperados:</h3>
     * <ul>
     *   <li>401: refresh_token expirado (después de 7 días, debe volver a login)</li>
     *   <li>401: refresh_token inválido o manipulado</li>
     * </ul>
     * 
     * @param request Contiene el refresh_token
     * @return AuthenticationResponse con nuevo access_token y mismos datos
     */
    @PostMapping("/refresh")
    @Operation(
        summary = "Renovar token de acceso (refresh)",
        description = "Genera nuevo access_token válido 24 horas usando refresh_token. " +
                     "No requiere credenciales. Refresh_token permanece válido 7 días.",
        tags = {"Autenticación"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token renovado exitosamente. Nuevo access_token válido 24 horas.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthenticationResponse.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Refresh token no proporcionado"),
        @ApiResponse(responseCode = "401", description = "Refresh token inválido, expirado o manipulado. Debe volver a hacer login.")
    })
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
                log.warn("Refresh token inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String username = jwtTokenProvider.getUsernameFromToken(request.getRefreshToken());
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            log.info("Refrescando token para usuario: {}", username);

            // Generar nuevo access token
            String newAccessToken = jwtTokenProvider.generateToken(username);

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtExpirationMs / 1000)
                    .username(username)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al refrescar token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * ENDPOINT: Validar token JWT
     * 
     * <h3>Descripción:</h3>
     * Verifica si un token JWT es válido, no está expirado y no ha sido manipulado.
     * Endpoint útil para verificar estado de sesión desde el cliente.
     * 
     * <h3>Retorna:</h3>
     * <ul>
     *   <li>true: Token válido y activo</li>
     *   <li>false: Token inválido, expirado o manipulado</li>
     * </ul>
     * 
     * @param token Token JWT sin el prefijo "Bearer" (solo el token)
     * @return true si es válido, false si no
     */
    @GetMapping("/validate")
    @Operation(
        summary = "Validar token JWT",
        description = "Verifica si un token JWT es válido, no está expirado y no ha sido manipulado. " +
                     "Retorna true/false indicando validez del token.",
        tags = {"Autenticación"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Validación completada. Retorna true o false."),
        @ApiResponse(responseCode = "400", description = "Token no proporcionado como parámetro")
    })
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        try {
            boolean isValid = jwtTokenProvider.validateToken(token);
            log.debug("Validación de token: {}", isValid);
            return ResponseEntity.ok(isValid);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return ResponseEntity.ok(false);
        }
    }

    /**
     * ENDPOINT: Logout del usuario
     * 
     * <h3>Descripción:</h3>
     * Cierra la sesión del usuario. En JWT stateless, principalmente informa al servidor.
     * El cliente debe eliminar el access_token de su almacenamiento local.
     * 
     * <h3>Seguridad:</h3>
     * En producción, el servidor podría:
     * <ul>
     *   <li>Mantener lista negra (blacklist) de tokens revocados</li>
     *   <li>Invalidar refresh_tokens en base de datos</li>
     *   <li>Registrar eventos de logout para auditoría</li>
     * </ul>
     * 
     * <h3>Acciones del cliente después de logout:</h3>
     * <ol>
     *   <li>Eliminar access_token del almacenamiento</li>
     *   <li>Eliminar refresh_token del almacenamiento</li>
     *   <li>Redirigir a página de login</li>
     *   <li>Limpiar datos de sesión del usuario</li>
     * </ol>
     * 
     * @return 200 OK indicando logout exitoso
     */
    @PostMapping("/logout")
    @Operation(
        summary = "Logout del usuario",
        description = "Cierra la sesión del usuario. Cliente debe eliminar tokens de su almacenamiento. " +
                     "En futuras peticiones usará login nuevamente.",
        tags = {"Autenticación"}
    )
    @ApiResponse(responseCode = "200", description = "Logout exitoso. Cliente debe eliminar tokens almacenados.")
    public ResponseEntity<Void> logout() {
        log.info("Usuario cerró sesión");
        // En una aplicación de producción, podrías:
        // - Añadir el token a una lista negra (blacklist)
        // - Invalidar tokens en base de datos
        // - Revocar permisos específicos
        return ResponseEntity.ok().build();
    }
}
