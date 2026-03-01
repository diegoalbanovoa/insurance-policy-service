package com.insurance.domain.model;

import com.insurance.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for User entity class and UserRole enum.
 */
@DisplayName("User Entity Tests")
@SuppressWarnings("all")
class UserTest {

    @Test
    @DisplayName("Should create user using builder")
    void shouldCreateUserUsingBuilder() {
        // When
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encryptedPassword123")
                .email("testuser@example.com")
                .role(User.UserRole.CUSTOMER)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
        
        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("encryptedPassword123", user.getPassword());
        assertEquals("testuser@example.com", user.getEmail());
        assertEquals(User.UserRole.CUSTOMER, user.getRole());
        assertTrue(user.getEnabled());
        assertTrue(user.getAccountNonExpired());
        assertTrue(user.getCredentialsNonExpired());
        assertTrue(user.getAccountNonLocked());
    }

    @Test
    @DisplayName("Should create user using no-args constructor")
    void shouldCreateUserUsingNoArgsConstructor() {
        // When
        User user = new User();
        
        // Then
        assertNotNull(user);
        assertNull(user.getId());
        assertNull(user.getEmail());
        assertNull(user.getPassword());
        assertNull(user.getRole());
    }

    @Test
    @DisplayName("Should set all properties using setters")
    void shouldSetAllPropertiesUsingSetters() {
        // Given
        User user = new User();
        
        // When
        user.setId(10L);
        user.setEmail("admin@example.com");
        user.setPassword("admin123");
        user.setEmail("admin@example.com");
        user.setRole(User.UserRole.ADMIN);
        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setCredentialsNonExpired(true);
        user.setAccountNonLocked(true);
        
        // Then
        assertEquals(10L, user.getId());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals("admin123", user.getPassword());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals(User.UserRole.ADMIN, user.getRole());
        assertTrue(user.getEnabled());
        assertTrue(user.getAccountNonExpired());
        assertTrue(user.getCredentialsNonExpired());
        assertTrue(user.getAccountNonLocked());
    }

    @Test
    @DisplayName("Should call onCreate when PrePersist is triggered")
    void shouldCallOnCreateWhenPrePersistIsTriggered() throws Exception {
        // Given
        User user = new User();
        Method onCreateMethod = User.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        
        // When
        onCreateMethod.invoke(user);
        
        // Then
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getUpdatedAt());
        assertEquals(user.getCreatedAt(), user.getUpdatedAt());
    }

    @Test
    @DisplayName("Should call onUpdate when PreUpdate is triggered")
    void shouldCallOnUpdateWhenPreUpdateIsTriggered() throws Exception {
        // Given
        User user = new User();
        Method onCreateMethod = User.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(user);
        
        LocalDateTime originalCreatedAt = user.getCreatedAt();
        LocalDateTime originalUpdatedAt = user.getUpdatedAt();
        
        Thread.sleep(10); // Small delay to ensure different timestamp
        
        Method onUpdateMethod = User.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        
        // When
        onUpdateMethod.invoke(user);
        
        // Then
        assertEquals(originalCreatedAt, user.getCreatedAt()); // createdAt should not change
        assertNotEquals(originalUpdatedAt, user.getUpdatedAt()); // updatedAt should change
        assertTrue(user.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should support equals and hashCode")
    void shouldSupportEqualsAndHashCode() {
        // Given
        User user1 = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .email("test@example.com")
                .role(User.UserRole.CUSTOMER)
                .enabled(true)
                .build();
        
        User user2 = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password123")
                .email("test@example.com")
                .role(User.UserRole.CUSTOMER)
                .enabled(true)
                .build();
        
        // Then
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("Should support toString method")
    void shouldSupportToStringMethod() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .email("test@example.com")
                .role(User.UserRole.AGENT)
                .build();
        
        // When
        String toString = user.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("test@example.com"));
    }

    @Test
    @DisplayName("Should handle disabled user account")
    void shouldHandleDisabledUserAccount() {
        // When
        User user = User.builder()
                .email("disabled@example.com")
                .password("password")
                .email("disabled@example.com")
                .role(User.UserRole.CUSTOMER)
                .enabled(false)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
        
        // Then
        assertFalse(user.getEnabled());
        assertTrue(user.getAccountNonExpired());
        assertTrue(user.getCredentialsNonExpired());
        assertTrue(user.getAccountNonLocked());
    }

    @Test
    @DisplayName("Should handle expired user account")
    void shouldHandleExpiredUserAccount() {
        // When
        User user = User.builder()
                .email("expired@example.com")
                .password("password")
                .email("expired@example.com")
                .role(User.UserRole.CUSTOMER)
                .enabled(true)
                .accountNonExpired(false)
                .credentialsNonExpired(false)
                .accountNonLocked(true)
                .build();
        
        // Then
        assertTrue(user.getEnabled());
        assertFalse(user.getAccountNonExpired());
        assertFalse(user.getCredentialsNonExpired());
        assertTrue(user.getAccountNonLocked());
    }

    @Test
    @DisplayName("Should handle locked user account")
    void shouldHandleLockedUserAccount() {
        // When
        User user = User.builder()
                .email("locked@example.com")
                .password("password")
                .email("locked@example.com")
                .role(User.UserRole.CUSTOMER)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(false)
                .build();
        
        // Then
        assertTrue(user.getEnabled());
        assertTrue(user.getAccountNonExpired());
        assertTrue(user.getCredentialsNonExpired());
        assertFalse(user.getAccountNonLocked());
    }

    // UserRole enum tests

    @Test
    @DisplayName("Should get authority for ADMIN role")
    void shouldGetAuthorityForAdminRole() {
        // When
        User.UserRole role = User.UserRole.ADMIN;
        
        // Then
        assertEquals("ROLE_ADMIN", role.getAuthority());
        assertEquals("Administrator with full access", role.getDescription());
    }

    @Test
    @DisplayName("Should get authority for AGENT role")
    void shouldGetAuthorityForAgentRole() {
        // When
        User.UserRole role = User.UserRole.AGENT;
        
        // Then
        assertEquals("ROLE_AGENT", role.getAuthority());
        assertEquals("Insurance agent", role.getDescription());
    }

    @Test
    @DisplayName("Should get authority for CUSTOMER role")
    void shouldGetAuthorityForCustomerRole() {
        // When
        User.UserRole role = User.UserRole.CUSTOMER;
        
        // Then
        assertEquals("ROLE_CUSTOMER", role.getAuthority());
        assertEquals("Customer with limited access", role.getDescription());
    }

    @Test
    @DisplayName("Should have exactly three user roles")
    void shouldHaveExactlyThreeUserRoles() {
        // When
        User.UserRole[] roles = User.UserRole.values();
        
        // Then
        assertEquals(3, roles.length);
        assertArrayEquals(
                new User.UserRole[]{User.UserRole.ADMIN, User.UserRole.AGENT, User.UserRole.CUSTOMER},
                roles
        );
    }

    @Test
    @DisplayName("Should create user with ADMIN role")
    void shouldCreateUserWithAdminRole() {
        // When
        User user = User.builder()
                .email("admin@test.com")
                .password("adminpass")
                .email("admin@example.com")
                .role(User.UserRole.ADMIN)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
        
        // Then
        assertEquals(User.UserRole.ADMIN, user.getRole());
        assertEquals("ROLE_ADMIN", user.getRole().getAuthority());
    }

    @Test
    @DisplayName("Should create user with AGENT role")
    void shouldCreateUserWithAgentRole() {
        // When
        User user = User.builder()
                .email("agent@test.com")
                .password("agentpass")
                .email("agent@example.com")
                .role(User.UserRole.AGENT)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
        
        // Then
        assertEquals(User.UserRole.AGENT, user.getRole());
        assertEquals("ROLE_AGENT", user.getRole().getAuthority());
    }

    @Test
    @DisplayName("Should create user with CUSTOMER role")
    void shouldCreateUserWithCustomerRole() {
        // When
        User user = User.builder()
                .email("customer@test.com")
                .password("customerpass")
                .email("customer@example.com")
                .role(User.UserRole.CUSTOMER)
                .enabled(true)
                .accountNonExpired(true)
                .credentialsNonExpired(true)
                .accountNonLocked(true)
                .build();
        
        // Then
        assertEquals(User.UserRole.CUSTOMER, user.getRole());
        assertEquals("ROLE_CUSTOMER", user.getRole().getAuthority());
    }

    @Test
    @DisplayName("Should convert role string to enum")
    void shouldConvertRoleStringToEnum() {
        // When
        User.UserRole adminRole = User.UserRole.valueOf("admin@test.com");
        User.UserRole agentRole = User.UserRole.valueOf("agent@test.com");
        User.UserRole customerRole = User.UserRole.valueOf("customer@test.com");
        
        // Then
        assertEquals(User.UserRole.ADMIN, adminRole);
        assertEquals(User.UserRole.AGENT, agentRole);
        assertEquals(User.UserRole.CUSTOMER, customerRole);
    }

    @Test
    @DisplayName("Should handle invalid role string conversion")
    void shouldHandleInvalidRoleStringConversion() {
        // Then
        assertThrows(IllegalArgumentException.class, () -> {
            User.UserRole.valueOf("INVALID_ROLE");
        });
    }
}
