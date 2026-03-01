package com.insurance.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ResourceNotFoundException class.
 */
@DisplayName("ResourceNotFoundException Tests")
class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("Should create exception with resource and ID")
    void shouldCreateExceptionWithResourceAndId() {
        // Given
        String resource = "Policy";
        Long id = 123L;
        String expectedMessage = "Policy no encontrado con ID: 123";
        
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(resource, id);
        
        // Then
        assertNotNull(exception);
        assertEquals(expectedMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with custom message")
    void shouldCreateExceptionWithCustomMessage() {
        // Given
        String customMessage = "Client with email 'test@example.com' not found";
        
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(customMessage);
        
        // Then
        assertNotNull(exception);
        assertEquals(customMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should extend InsuranceException")
    void shouldExtendInsuranceException() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Client", 1L);
        
        // Then
        assertTrue(exception instanceof InsuranceException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should format message correctly with different resource types")
    void shouldFormatMessageCorrectlyWithDifferentResourceTypes() {
        // When
        ResourceNotFoundException policyException = new ResourceNotFoundException("Policy", 100L);
        ResourceNotFoundException clientException = new ResourceNotFoundException("Client", 200L);
        ResourceNotFoundException vehicleException = new ResourceNotFoundException("Vehicle", 300L);
        
        // Then
        assertEquals("Policy no encontrado con ID: 100", policyException.getMessage());
        assertEquals("Client no encontrado con ID: 200", clientException.getMessage());
        assertEquals("Vehicle no encontrado con ID: 300", vehicleException.getMessage());
    }

    @Test
    @DisplayName("Should handle null resource name gracefully")
    void shouldHandleNullResourceNameGracefully() {
        // Given
        String resource = null;
        Long id = 456L;
        
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(resource, id);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("null"));
        assertTrue(exception.getMessage().contains("456"));
    }

    @Test
    @DisplayName("Should handle null ID gracefully")
    void shouldHandleNullIdGracefully() {
        // Given
        String resource = "Policy";
        Long id = null;
        
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(resource, id);
        
        // Then
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Policy"));
        assertTrue(exception.getMessage().contains("null"));
    }

    @Test
    @DisplayName("Should accept null custom message")
    void shouldAcceptNullCustomMessage() {
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException(null);
        
        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        ResourceNotFoundException exception = new ResourceNotFoundException("User", 1L);
        
        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should be thrown and caught as ResourceNotFoundException")
    void shouldBeThrownAndCaughtAsResourceNotFoundException() {
        // Given
        String resource = "Client";
        Long id = 999L;
        
        // When & Then
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            throw new ResourceNotFoundException(resource, id);
        });
        
        assertTrue(thrown.getMessage().contains(resource));
        assertTrue(thrown.getMessage().contains(id.toString()));
    }

    @Test
    @DisplayName("Should be caught as InsuranceException")
    void shouldBeCaughtAsInsuranceException() {
        // Given
        String message = "Resource not found";
        
        // When & Then
        InsuranceException thrown = assertThrows(InsuranceException.class, () -> {
            throw new ResourceNotFoundException(message);
        });
        
        assertTrue(thrown instanceof ResourceNotFoundException);
        assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Should create different exceptions for different resources")
    void shouldCreateDifferentExceptionsForDifferentResources() {
        // When
        ResourceNotFoundException exception1 = new ResourceNotFoundException("Policy", 1L);
        ResourceNotFoundException exception2 = new ResourceNotFoundException("Client", 1L);
        
        // Then
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertTrue(exception1.getMessage().contains("Policy"));
        assertTrue(exception2.getMessage().contains("Client"));
    }

    @Test
    @DisplayName("Should create different exceptions for different IDs")
    void shouldCreateDifferentExceptionsForDifferentIds() {
        // When
        ResourceNotFoundException exception1 = new ResourceNotFoundException("Policy", 1L);
        ResourceNotFoundException exception2 = new ResourceNotFoundException("Policy", 2L);
        
        // Then
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertTrue(exception1.getMessage().contains("1"));
        assertTrue(exception2.getMessage().contains("2"));
    }
}
