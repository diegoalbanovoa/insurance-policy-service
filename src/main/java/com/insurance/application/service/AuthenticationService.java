package com.insurance.application.service;

import com.insurance.application.dto.auth.AuthenticationRequest;
import com.insurance.application.dto.auth.AuthenticationResponse;
import com.insurance.application.dto.auth.RegistrationRequest;
import com.insurance.domain.entity.User;
import com.insurance.domain.repository.IUserRepository;
import com.insurance.infrastructure.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    /**
     * Autentica a un usuario con email y contraseña
     * @param request Credenciales del usuario
     * @return AuthenticationResponse con tokens
     * @throws BadCredentialsException si las credenciales son inválidas
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Autenticando usuario: {}", request.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

            log.info("Autenticación exitosa para: {}", request.getEmail());

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpirationMs / 1000)
                    .username(request.getEmail())
                    .email(request.getEmail())
                    .build();

        } catch (Exception e) {
            log.warn("Fallo en autenticación para {}: {}", request.getEmail(), e.getMessage());
            throw new BadCredentialsException("Credenciales inválidas", e);
        }
    }

    /**
     * Registra un nuevo usuario
     * @param request Datos de registro
     * @return AuthenticationResponse con tokens para el nuevo usuario
     * @throws IllegalArgumentException si el email ya existe o las contraseñas no coinciden
     */
    @Transactional
    public AuthenticationResponse register(RegistrationRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getEmail());

        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            log.warn("Las contraseñas no coinciden para: {}", request.getEmail());
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        // Validar que el email no exista
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            log.warn("El email ya está registrado: {}", request.getEmail());
            throw new IllegalArgumentException("El email ya está registrado");
        }

        // Crear nuevo usuario
        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .role(User.UserRole.CUSTOMER) // Rol por defecto para nuevos usuarios
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Usuario registrado exitosamente: {}", savedUser.getEmail());

        // Generar tokens automáticamente para el nuevo usuario
        String accessToken = jwtTokenProvider.generateToken(request.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpirationMs / 1000)
                .username(savedUser.getEmail())
                .email(savedUser.getEmail())
                .build();
    }

    /**
     * Obtiene información del usuario autenticado actual
     * @param email Email del usuario
     * @return Datos básicos del usuario
     */
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    /**
     * Verifica si un usuario existe
     * @param email Email a verificar
     * @return true si existe, false en otro caso
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }
}
