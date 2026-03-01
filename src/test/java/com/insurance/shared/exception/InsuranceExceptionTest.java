package com.insurance.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for InsuranceException class.
 */
@DisplayName("InsuranceException Tests")
class InsuranceExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void shouldCreateExceptionWithMessage() {
        // Given
        String errorMessage = "An insurance error occurred";
        
        // When
        InsuranceException exception = new InsuranceException(errorMessage);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should create exception with message and cause")
    void shouldCreateExceptionWithMessageAndCause() {
        // Given
        String errorMessage = "An insurance error occurred";
        Throwable cause = new RuntimeException("Root cause");
        
        // When
        InsuranceException exception = new InsuranceException(errorMessage, cause);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Root cause", exception.getCause().getMessage());
    }

    @Test
    @DisplayName("Should be instance of RuntimeException")
    void shouldBeInstanceOfRuntimeException() {
        // Given
        InsuranceException exception = new InsuranceException("Test");
        
        // Then
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        InsuranceException exception = new InsuranceException("Test error");
        
        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
    }

    @Test
    @DisplayName("Should accept null message")
    void shouldAcceptNullMessage() {
        // When
        InsuranceException exception = new InsuranceException(null);
        
        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should accept null cause")
    void shouldAcceptNullCause() {
        // Given
        String message = "Error message";
        
        // When
        InsuranceException exception = new InsuranceException(message, null);
        
        // Then
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should chain exceptions properly")
    void shouldChainExceptionsProperly() {
        // Given
        Throwable rootCause = new IllegalArgumentException("Invalid argument");
        Throwable intermediateCause = new RuntimeException("Processing failed", rootCause);
        
        // When
        InsuranceException exception = new InsuranceException("Insurance operation failed", intermediateCause);
        
        // Then
        assertEquals("Insurance operation failed", exception.getMessage());
        assertEquals(intermediateCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }
}
