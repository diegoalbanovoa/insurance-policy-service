package com.insurance.application.service;

import com.insurance.application.dto.auth.AuthenticationRequest;
import com.insurance.application.dto.auth.AuthenticationResponse;
import com.insurance.application.dto.auth.RegistrationRequest;
import com.insurance.domain.entity.User;
import com.insurance.domain.repository.IUserRepository;
import com.insurance.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthenticationService authenticationService;

    private User testUser;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                authenticationManager,
                jwtTokenProvider,
                userRepository,
                passwordEncoder
        );

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(User.UserRole.CUSTOMER)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    void testAuthenticate_Success() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuth);
        when(jwtTokenProvider.generateToken(mockAuth)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("test@example.com")).thenReturn("refresh-token");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("test@example.com", response.getEmail());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on invalid credentials")
    void testAuthenticate_InvalidCredentials() {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@example.com")
                .password("wrongpassword")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Invalid credentials") {});

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> {
            authenticationService.authenticate(request);
        });
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegister_Success() {
        // Arrange
        RegistrationRequest request = RegistrationRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .fullName("New User")
                .build();

        when(userRepository.existsByEmailIgnoreCase("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtTokenProvider.generateToken("newuser@example.com")).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("newuser@example.com")).thenReturn("refresh-token");

        // Act
        AuthenticationResponse response = authenticationService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration with non-matching passwords")
    void testRegister_PasswordMismatch() {
        // Arrange
        RegistrationRequest request = RegistrationRequest.builder()
                .email("newuser@example.com")
                .password("Password123!")
                .confirmPassword("DifferentPassword123!")
                .fullName("New User")
                .build();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(request);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration with existing email")
    void testRegister_EmailAlreadyExists() {
        // Arrange
        RegistrationRequest request = RegistrationRequest.builder()
                .email("test@example.com")
                .password("Password123!")
                .confirmPassword("Password123!")
                .fullName("Test User")
                .build();

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(request);
        });
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void testGetUserByEmail_Success() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act
        Optional<User> user = authenticationService.getUserByEmail("test@example.com");

        // Assert
        assertTrue(user.isPresent());
        assertEquals("test@example.com", user.get().getEmail());
        verify(userRepository, times(1)).findByEmailIgnoreCase("test@example.com");
    }

    @Test
    @DisplayName("Should return empty Optional when user not found")
    void testGetUserByEmail_UserNotFound() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // Act
        Optional<User> user = authenticationService.getUserByEmail("nonexistent@example.com");

        // Assert
        assertFalse(user.isPresent());
        verify(userRepository, times(1)).findByEmailIgnoreCase("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should verify user exists")
    void testUserExists_True() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        // Act
        boolean exists = authenticationService.userExists("test@example.com");

        // Assert
        assertTrue(exists);
        verify(userRepository, times(1)).existsByEmailIgnoreCase("test@example.com");
    }

    @Test
    @DisplayName("Should verify user not exists")
    void testUserExists_False() {
        // Arrange
        when(userRepository.existsByEmailIgnoreCase("nonexistent@example.com")).thenReturn(false);

        // Act
        boolean exists = authenticationService.userExists("nonexistent@example.com");

        // Assert
        assertFalse(exists);
        verify(userRepository, times(1)).existsByEmailIgnoreCase("nonexistent@example.com");
    }
}
