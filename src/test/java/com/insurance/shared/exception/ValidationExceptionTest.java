package com.insurance.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ValidationException class.
 */
@DisplayName("ValidationException Tests")
class ValidationExceptionTest {

    @Test
    @DisplayName("Should create validation exception with message")
    void shouldCreateValidationExceptionWithMessage() {
        // Given
        String errorMessage = "Validation failed for field 'email'";
        
        // When
        ValidationException exception = new ValidationException(errorMessage);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should extend InsuranceException")
    void shouldExtendInsuranceException() {
        // Given
        ValidationException exception = new ValidationException("Validation error");
        
        // Then
        assertTrue(exception instanceof InsuranceException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should create exception with validation message")
    void shouldCreateExceptionWithValidationMessage() {
        // Given
        String field = "email";
        String constraint = "must not be null";
        String message = String.format("Validation error for field '%s': %s", field, constraint);
        
        // When
        ValidationException exception = new ValidationException(message);
        
        // Then
        assertEquals(message, exception.getMessage());
        assertTrue(exception.getMessage().contains(field));
        assertTrue(exception.getMessage().contains(constraint));
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        ValidationException exception = new ValidationException("Validation failed");
        
        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
        assertEquals(this.getClass().getName(), exception.getStackTrace()[0].getClassName());
    }

    @Test
    @DisplayName("Should accept null message")
    void shouldAcceptNullMessage() {
        // When
        ValidationException exception = new ValidationException(null);
        
        // Then
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should create exception with empty message")
    void shouldCreateExceptionWithEmptyMessage() {
        // Given
        String emptyMessage = "";
        
        // When
        ValidationException exception = new ValidationException(emptyMessage);
        
        // Then
        assertNotNull(exception);
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should create distinct exceptions for different validation errors")
    void shouldCreateDistinctExceptionsForDifferentValidationErrors() {
        // Given
        String emailError = "Email format is invalid";
        String phoneError = "Phone number format is invalid";
        
        // When
        ValidationException emailException = new ValidationException(emailError);
        ValidationException phoneException = new ValidationException(phoneError);
        
        // Then
        assertNotEquals(emailException.getMessage(), phoneException.getMessage());
        assertEquals(emailError, emailException.getMessage());
        assertEquals(phoneError, phoneException.getMessage());
    }

    @Test
    @DisplayName("Should be thrown and caught as ValidationException")
    void shouldBeThrownAndCaughtAsValidationException() {
        // Given
        String message = "Invalid input";
        
        // When & Then
        ValidationException thrown = assertThrows(ValidationException.class, () -> {
            throw new ValidationException(message);
        });
        
        assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Should be caught as InsuranceException")
    void shouldBeCaughtAsInsuranceException() {
        // Given
        String message = "Validation rule violated";
        
        // When & Then
        InsuranceException thrown = assertThrows(InsuranceException.class, () -> {
            throw new ValidationException(message);
        });
        
        assertTrue(thrown instanceof ValidationException);
        assertEquals(message, thrown.getMessage());
    }
}
