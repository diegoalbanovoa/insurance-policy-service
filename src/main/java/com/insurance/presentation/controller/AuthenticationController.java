package com.insurance.presentation.controller;

import com.insurance.application.dto.auth.AuthenticationRequest;
import com.insurance.application.dto.auth.AuthenticationResponse;
import com.insurance.application.dto.auth.RefreshTokenRequest;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints para autenticación y manejo de tokens JWT")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Endpoint para login con credenciales
     * @param request Email y contraseña
     * @return Token JWT y refresh token
     */
    @PostMapping("/login")
    @Operation(summary = "Login con credenciales", description = "Autentica un usuario y retorna JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Autenticación exitosa",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        try {
            log.info("Intento de login para usuario: {}", request.getEmail());

            // Autenticar con el AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Generar tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

            log.info("Login exitoso para usuario: {}", request.getEmail());

            // Construir respuesta
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpirationMs / 1000) // Convertir a segundos
                    .username(request.getEmail())
                    .email(request.getEmail())
                    .build();

            return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            log.warn("Fallo en autenticación: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthenticationResponse.builder()
                            .build());
        } catch (Exception e) {
            log.error("Error durante la autenticación: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AuthenticationResponse.builder()
                            .build());
        }
    }

    /**
     * Endpoint para refrescar el token JWT
     * @param request Refresh token
     * @return Nuevo access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token JWT", description = "Genera un nuevo access token usando el refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refrescado exitosamente",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado")
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
     * Endpoint para validar el token JWT actual
     * @param token Token JWT a validar (sin Bearer prefix)
     * @return true/false indicando si el token es válido
     */
    @GetMapping("/validate")
    @Operation(summary = "Validar token JWT", description = "Verifica si un token JWT es válido y no está expirado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validación completada"),
            @ApiResponse(responseCode = "400", description = "Token no proporcionado")
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
     * Endpoint para logout (es principalmente informativo en JWT)
     * En una aplicación real, podrías mantener una lista negra de tokens
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalida la sesión del usuario (basado en cliente borrar token)")
    @ApiResponse(responseCode = "200", description = "Logout exitoso")
    public ResponseEntity<Void> logout() {
        log.info("Usuario cerró sesión");
        // En una aplicación de producción, podrías:
        // - Añadir el token a una lista negra (blacklist)
        // - Invalidar tokens en base de datos
        // - Revocar permisos específicos
        return ResponseEntity.ok().build();
    }
}
