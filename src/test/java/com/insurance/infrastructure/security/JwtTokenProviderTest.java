package com.insurance.infrastructure.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@TestPropertySource(properties = {
        "app.jwt.secret=MyTestInsuranceServiceSuperSecureKeyFor512BitsHashing2024TestingEnvironment!@#",
        "app.jwt.expiration=3600000",
        "app.jwt.refresh-expiration=86400000"
})
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        mockAuthentication = mock(Authentication.class);
        when(mockAuthentication.getName()).thenReturn("test@example.com");
    }

    @Test
    @DisplayName("Should generate valid token from Authentication")
    void testGenerateToken_FromAuthentication() {
        // Act
        String token = jwtTokenProvider.generateToken(mockAuthentication);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should generate valid token from username")
    void testGenerateToken_FromUsername() {
        // Act
        String token = jwtTokenProvider.generateToken("test@example.com");

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should extract username from valid token")
    void testGetUsernameFromToken_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken("test@example.com");

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(token);

        // Assert
        assertNotNull(username);
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Should return null for invalid token")
    void testGetUsernameFromToken_InvalidToken() {
        // Act
        String username = jwtTokenProvider.getUsernameFromToken("invalid.token.here");

        // Assert
        assertNull(username);
    }

    @Test
    @DisplayName("Should validate valid token")
    void testValidateToken_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken("test@example.com");

        // Act
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should invalidate malformed token")
    void testValidateToken_MalformedToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("malformed.token");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should invalidate empty token")
    void testValidateToken_EmptyToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should invalidate null token")
    void testValidateToken_NullToken() {
        // Act
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should generate valid refresh token")
    void testGenerateRefreshToken_Success() {
        // Act
        String refreshToken = jwtTokenProvider.generateRefreshToken("test@example.com");

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
    }

    @Test
    @DisplayName("Should extract username from refresh token")
    void testGetUsernameFromToken_RefreshToken() {
        // Arrange
        String refreshToken = jwtTokenProvider.generateRefreshToken("test@example.com");

        // Act
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    @DisplayName("Should extract claims from valid token")
    void testGetClaimsFromToken_ValidToken() {
        // Arrange
        String token = jwtTokenProvider.generateToken("test@example.com");

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertEquals("test@example.com", claims.getSubject());
    }

    @Test
    @DisplayName("Should return null claims for invalid token")
    void testGetClaimsFromToken_InvalidToken() {
        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken("invalid.token.here");

        // Assert
        assertNull(claims);
    }

    @Test
    @DisplayName("Should token contain expiration date")
    void testGenerateToken_ContainsExpiration() {
        // Arrange
        String token = jwtTokenProvider.generateToken("test@example.com");

        // Act
        Claims claims = jwtTokenProvider.getClaimsFromToken(token);

        // Assert
        assertNotNull(claims);
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
    }

    @Test
    @DisplayName("Should different tokens for different users")
    void testGenerateToken_DifferentTokensForDifferentUsers() {
        // Act
        String token1 = jwtTokenProvider.generateToken("user1@example.com");
        String token2 = jwtTokenProvider.generateToken("user2@example.com");

        // Assert
        assertNotEquals(token1, token2);
        assertEquals("user1@example.com", jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals("user2@example.com", jwtTokenProvider.getUsernameFromToken(token2));
    }

    @Test
    @DisplayName("Should access/refresh tokens have different expiration")
    void testTokenExpiration_AccessVsRefresh() {
        // Arrange
        String accessToken = jwtTokenProvider.generateToken("test@example.com");
        String refreshToken = jwtTokenProvider.generateRefreshToken("test@example.com");

        // Act
        Claims accessClaims = jwtTokenProvider.getClaimsFromToken(accessToken);
        Claims refreshClaims = jwtTokenProvider.getClaimsFromToken(refreshToken);

        // Assert
        assertNotNull(accessClaims);
        assertNotNull(refreshClaims);
        // Refresh token expiration should be longer
        assertTrue(refreshClaims.getExpiration().getTime() > accessClaims.getExpiration().getTime());
    }
}
