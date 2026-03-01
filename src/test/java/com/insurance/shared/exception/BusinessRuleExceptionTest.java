package com.insurance.shared.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessRuleException class.
 */
@DisplayName("BusinessRuleException Tests")
class BusinessRuleExceptionTest {

    @Test
    @DisplayName("Should create business rule exception with message")
    void shouldCreateBusinessRuleExceptionWithMessage() {
        // Given
        String errorMessage = "Policy cannot be issued to clients under 18 years old";
        
        // When
        BusinessRuleException exception = new BusinessRuleException(errorMessage);
        
        // Then
        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    @DisplayName("Should extend InsuranceException")
    void shouldExtendInsuranceException() {
        // Given
        BusinessRuleException exception = new BusinessRuleException("Business rule violation");
        
        // Then
        assertTrue(exception instanceof InsuranceException);
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    @DisplayName("Should create exception for different business rules")
    void shouldCreateExceptionForDifferentBusinessRules() {
        // Given
        String ageRule = "Client must be at least 18 years old";
        String duplicateRule = "Policy number already exists";
        String dateRule = "End date must be after start date";
        
        // When
        BusinessRuleException ageException = new BusinessRuleException(ageRule);
        BusinessRuleException duplicateException = new BusinessRuleException(duplicateRule);
        BusinessRuleException dateException = new BusinessRuleException(dateRule);
        
        // Then
        assertEquals(ageRule, ageException.getMessage());
        assertEquals(duplicateRule, duplicateException.getMessage());
        assertEquals(dateRule, dateException.getMessage());
    }

    @Test
    @DisplayName("Should preserve stack trace")
    void shouldPreserveStackTrace() {
        // When
        BusinessRuleException exception = new BusinessRuleException("Rule violation");
        
        // Then
        assertNotNull(exception.getStackTrace());
        assertTrue(exception.getStackTrace().length > 0);
        assertEquals(this.getClass().getName(), exception.getStackTrace()[0].getClassName());
    }

    @Test
    @DisplayName("Should accept null message")
    void shouldAcceptNullMessage() {
        // When
        BusinessRuleException exception = new BusinessRuleException(null);
        
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
        BusinessRuleException exception = new BusinessRuleException(emptyMessage);
        
        // Then
        assertNotNull(exception);
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should be thrown and caught as BusinessRuleException")
    void shouldBeThrownAndCaughtAsBusinessRuleException() {
        // Given
        String ruleMessage = "Premium amount must be greater than zero";
        
        // When & Then
        BusinessRuleException thrown = assertThrows(BusinessRuleException.class, () -> {
            throw new BusinessRuleException(ruleMessage);
        });
        
        assertEquals(ruleMessage, thrown.getMessage());
    }

    @Test
    @DisplayName("Should be caught as InsuranceException")
    void shouldBeCaughtAsInsuranceException() {
        // Given
        String message = "Business constraint violated";
        
        // When & Then
        InsuranceException thrown = assertThrows(InsuranceException.class, () -> {
            throw new BusinessRuleException(message);
        });
        
        assertTrue(thrown instanceof BusinessRuleException);
        assertEquals(message, thrown.getMessage());
    }

    @Test
    @DisplayName("Should create distinct exceptions for different rules")
    void shouldCreateDistinctExceptionsForDifferentRules() {
        // Given
        String rule1 = "Policy start date cannot be in the past";
        String rule2 = "Policy duration cannot exceed 10 years";
        
        // When
        BusinessRuleException exception1 = new BusinessRuleException(rule1);
        BusinessRuleException exception2 = new BusinessRuleException(rule2);
        
        // Then
        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertEquals(rule1, exception1.getMessage());
        assertEquals(rule2, exception2.getMessage());
    }

    @Test
    @DisplayName("Should support complex business rule messages")
    void shouldSupportComplexBusinessRuleMessages() {
        // Given
        String complexRule = "Client with tipo_documento='CC' and numero_documento='12345' " +
                           "already has an active policy of type 'AUTO'";
        
        // When
        BusinessRuleException exception = new BusinessRuleException(complexRule);
        
        // Then
        assertEquals(complexRule, exception.getMessage());
        assertTrue(exception.getMessage().contains("tipo_documento"));
        assertTrue(exception.getMessage().contains("numero_documento"));
        assertTrue(exception.getMessage().contains("AUTO"));
    }
}
