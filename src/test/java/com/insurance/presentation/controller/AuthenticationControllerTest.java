package com.insurance.presentation.controller;

import com.insurance.application.dto.auth.AuthenticationRequest;
import com.insurance.application.dto.auth.AuthenticationResponse;
import com.insurance.application.dto.auth.RefreshTokenRequest;
import com.insurance.application.dto.auth.RegistrationRequest;
import com.insurance.application.service.AuthenticationService;
import com.insurance.infrastructure.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private AuthenticationService authenticationService;

    private AuthenticationRequest authRequest;
    private AuthenticationResponse authResponse;
    private RegistrationRequest regRequest;
    private RefreshTokenRequest refreshRequest;

    @BeforeEach
    void setUp() {
        authRequest = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        authResponse = AuthenticationResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .username("test@example.com")
                .email("test@example.com")
                .build();

        regRequest = RegistrationRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .fullName("New User")
                .build();

        refreshRequest = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void testLogin_Success() throws Exception {
        // Arrange
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return 401 for invalid credentials")
    void testLogin_InvalidCredentials() throws Exception {
        // Arrange
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 for missing email")
    void testLogin_MissingEmail() throws Exception {
        // Arrange
        AuthenticationRequest invalidRequest = AuthenticationRequest.builder()
                .password("password")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should register successfully with valid data")
    void testRegister_Success() throws Exception {
        // Arrange
        when(authenticationService.register(any(RegistrationRequest.class)))
                .thenReturn(authResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente. Puede iniciar sesión inmediatamente."))
                .andExpect(jsonPath("$.data.access_token").value("access-token"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));
    }

    @Test
    @DisplayName("Should return 400 for password mismatch during registration")
    void testRegister_PasswordMismatch() throws Exception {
        // Arrange
        when(authenticationService.register(any(RegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException("Las contraseñas no coinciden"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should return 400 for existing email during registration")
    void testRegister_EmailAlreadyExists() throws Exception {
        // Arrange
        when(authenticationService.register(any(RegistrationRequest.class)))
                .thenThrow(new IllegalArgumentException("El email ya está registrado"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("El email ya está registrado"));
    }

    @Test
    @DisplayName("Should return 400 for short password during registration")
    void testRegister_PasswordTooShort() throws Exception {
        // Arrange
        RegistrationRequest shortPassRequest = RegistrationRequest.builder()
                .email("newuser@example.com")
                .password("short")
                .confirmPassword("short")
                .fullName("New User")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(shortPassRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should refresh token successfully")
    void testRefresh_Success() throws Exception {
        // Arrange
        when(jwtTokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("refresh-token")).thenReturn("test@example.com");
        when(jwtTokenProvider.generateToken("test@example.com")).thenReturn("new-access-token");

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access-token"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token"));
    }

    @Test
    @DisplayName("Should return 401 for invalid refresh token")
    void testRefresh_InvalidToken() throws Exception {
        // Arrange
        when(jwtTokenProvider.validateToken("invalid-refresh-token")).thenReturn(false);
        RefreshTokenRequest invalidRequest = RefreshTokenRequest.builder()
                .refreshToken("invalid-refresh-token")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate token successfully")
    void testValidate_ValidToken() throws Exception {
        // Arrange
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/validate")
                .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("Should return false for invalid token validation")
    void testValidate_InvalidToken() throws Exception {
        // Arrange
        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v1/auth/validate")
                .param("token", "invalid-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Should logout successfully")
    void testLogout_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk());
    }
}
