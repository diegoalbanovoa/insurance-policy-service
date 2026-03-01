package com.insurance.infrastructure.security;

import com.insurance.domain.entity.User;
import com.insurance.domain.repository.IUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private IUserRepository userRepository;

    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        customUserDetailsService = new CustomUserDetailsService(userRepository);
        
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
    @DisplayName("Should load user by username successfully")
    void testLoadUserByUsername_Success() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("test@example.com"))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertEquals(1, userDetails.getAuthorities().size());
        verify(userRepository, times(1)).findByEmailIgnoreCase("test@example.com");
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user not found")
    void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("nonexistent@example.com"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistent@example.com");
        });
        verify(userRepository, times(1)).findByEmailIgnoreCase("nonexistent@example.com");
    }

    @Test
    @DisplayName("Should load user by ID successfully")
    void testLoadUserById_Success() {
        // Arrange
        when(userRepository.findByIdAndEnabled(1L))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserById(1L);

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        verify(userRepository, times(1)).findByIdAndEnabled(1L);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user ID not found")
    void testLoadUserById_UserNotFound() {
        // Arrange
        when(userRepository.findByIdAndEnabled(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserById(999L);
        });
        verify(userRepository, times(1)).findByIdAndEnabled(999L);
    }

    @Test
    @DisplayName("Should load user with ADMIN role")
    void testLoadUserByUsername_AdminRole() {
        // Arrange
        User adminUser = User.builder()
                .id(2L)
                .email("admin@example.com")
                .password("encodedPassword")
                .fullName("Admin User")
                .role(User.UserRole.ADMIN)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmailIgnoreCase("admin@example.com"))
                .thenReturn(Optional.of(adminUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("admin@example.com");

        // Assert
        assertNotNull(userDetails);
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should load disabled user as disabled")
    void testLoadUserByUsername_DisabledUser() {
        // Arrange
        User disabledUser = User.builder()
                .id(3L)
                .email("disabled@example.com")
                .password("encodedPassword")
                .fullName("Disabled User")
                .role(User.UserRole.CUSTOMER)
                .enabled(false)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmailIgnoreCase("disabled@example.com"))
                .thenReturn(Optional.of(disabledUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("disabled@example.com");

        // Assert
        assertNotNull(userDetails);
        assertFalse(userDetails.isEnabled());
    }

    @Test
    @DisplayName("Should handle case-insensitive email lookup")
    void testLoadUserByUsername_CaseInsensitive() {
        // Arrange
        when(userRepository.findByEmailIgnoreCase("TEST@EXAMPLE.COM"))
                .thenReturn(Optional.of(testUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("TEST@EXAMPLE.COM");

        // Assert
        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        verify(userRepository, times(1)).findByEmailIgnoreCase("TEST@EXAMPLE.COM");
    }
}
