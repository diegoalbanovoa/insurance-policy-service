package com.insurance.infrastructure.config;

import com.insurance.application.dto.ErrorResponse;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.InsuranceException;
import com.insurance.shared.exception.ResourceNotFoundException;
import com.insurance.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
@SuppressWarnings("all")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException with 404 status")
    void shouldHandleResourceNotFoundExceptionWith404Status() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Policy", 123L);
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(404, errorResponse.getStatus());
        assertEquals("Policy no encontrado con ID: 123", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should handle ResourceNotFoundException with custom message")
    void shouldHandleResourceNotFoundExceptionWithCustomMessage() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Custom resource not found");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(404, errorResponse.getStatus());
        assertEquals("Custom resource not found", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
    }

    @Test
    @DisplayName("Should handle ValidationException with 400 status")
    void shouldHandleValidationExceptionWith400Status() {
        // Given
        ValidationException exception = new ValidationException("Invalid email format");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Invalid email format", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should handle ValidationException with multiple validation errors")
    void shouldHandleValidationExceptionWithMultipleErrors() {
        // Given
        ValidationException exception = new ValidationException("Field 'name' is required; Field 'email' must be valid");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("name"));
        assertTrue(errorResponse.getMessage().contains("email"));
    }

    @Test
    @DisplayName("Should handle BusinessRuleException with 400 status")
    void shouldHandleBusinessRuleExceptionWith400Status() {
        // Given
        BusinessRuleException exception = new BusinessRuleException("Policy premium amount must be positive");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessRuleException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Policy premium amount must be positive", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should handle BusinessRuleException with complex business rule")
    void shouldHandleBusinessRuleExceptionWithComplexRule() {
        // Given
        BusinessRuleException exception = new BusinessRuleException(
                "Total beneficiary percentage cannot exceed 100%. Current total: 125%");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessRuleException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(400, errorResponse.getStatus());
        assertTrue(errorResponse.getMessage().contains("percentage"));
        assertTrue(errorResponse.getMessage().contains("125%"));
    }

    @Test
    @DisplayName("Should handle InsuranceException with 500 status")
    void shouldHandleInsuranceExceptionWith500Status() {
        // Given
        InsuranceException exception = new InsuranceException("Internal domain error");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsuranceException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Internal domain error", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should handle InsuranceException with underlying cause")
    void shouldHandleInsuranceExceptionWithCause() {
        // Given
        Throwable cause = new RuntimeException("Database connection failed");
        InsuranceException exception = new InsuranceException("Failed to process policy", cause);
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleInsuranceException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Failed to process policy", errorResponse.getMessage());
    }

    @Test
    @DisplayName("Should handle generic Exception with 500 status")
    void shouldHandleGenericExceptionWith500Status() {
        // Given
        Exception exception = new RuntimeException("Unexpected error occurred");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Ha ocurrido un error inesperado. Por favor intente más tarde.", errorResponse.getMessage());
        assertEquals("/api/test", errorResponse.getPath());
        assertEquals("Unexpected error occurred", errorResponse.getDetails());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    @DisplayName("Should handle NullPointerException as generic exception")
    void shouldHandleNullPointerExceptionAsGenericException() {
        // Given
        NullPointerException exception = new NullPointerException("Null value encountered");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGlobalException(exception, webRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(500, errorResponse.getStatus());
        assertEquals("Null value encountered", errorResponse.getDetails());
    }

    @Test
    @DisplayName("Should extract path correctly from WebRequest description")
    void shouldExtractPathCorrectlyFromWebRequest() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=/api/clients/123");
        ValidationException exception = new ValidationException("Validation failed");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, webRequest);
        
        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("/api/clients/123", errorResponse.getPath());
    }

    @Test
    @DisplayName("Should handle path with query parameters")
    void shouldHandlePathWithQueryParameters() {
        // Given
        when(webRequest.getDescription(false)).thenReturn("uri=/api/policies?status=ACTIVE&page=1");
        ResourceNotFoundException exception = new ResourceNotFoundException("Policy", 999L);
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleResourceNotFound(exception, webRequest);
        
        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("/api/policies?status=ACTIVE&page=1", errorResponse.getPath());
    }

    @Test
    @DisplayName("Should set timestamp close to current time")
    void shouldSetTimestampCloseToCurrentTime() {
        // Given
        ValidationException exception = new ValidationException("Test validation");
        
        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationException(exception, webRequest);
        
        // Then
        ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertNotNull(errorResponse.getTimestamp());
        
        // Timestamp should be within last few seconds
        long secondsAgo = java.time.Duration.between(
                errorResponse.getTimestamp(),
                java.time.LocalDateTime.now()
        ).getSeconds();
        
        assertTrue(secondsAgo < 5, "Timestamp should be recent");
    }

    @Test
    @DisplayName("Should return consistent error structure for all exception types")
    void shouldReturnConsistentErrorStructureForAllExceptions() {
        // Given
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Client", 1L);
        ValidationException validationEx = new ValidationException("Invalid data");
        BusinessRuleException businessEx = new BusinessRuleException("Rule violation");
        InsuranceException insuranceEx = new InsuranceException("Domain error");
        Exception genericEx = new RuntimeException("Unexpected");
        
        // When
        ResponseEntity<ErrorResponse> notFoundResponse = exceptionHandler.handleResourceNotFound(notFoundEx, webRequest);
        ResponseEntity<ErrorResponse> validationResponse = exceptionHandler.handleValidationException(validationEx, webRequest);
        ResponseEntity<ErrorResponse> businessResponse = exceptionHandler.handleBusinessRuleException(businessEx, webRequest);
        ResponseEntity<ErrorResponse> insuranceResponse = exceptionHandler.handleInsuranceException(insuranceEx, webRequest);
        ResponseEntity<ErrorResponse> genericResponse = exceptionHandler.handleGlobalException(genericEx, webRequest);
        
        // Then - All should have consistent structure
        assertAll("All error responses should have required fields",
                () -> assertNotNull(notFoundResponse.getBody().getStatus()),
                () -> assertNotNull(notFoundResponse.getBody().getMessage()),
                () -> assertNotNull(notFoundResponse.getBody().getPath()),
                () -> assertNotNull(notFoundResponse.getBody().getTimestamp()),
                
                () -> assertNotNull(validationResponse.getBody().getStatus()),
                () -> assertNotNull(validationResponse.getBody().getMessage()),
                () -> assertNotNull(validationResponse.getBody().getPath()),
                () -> assertNotNull(validationResponse.getBody().getTimestamp()),
                
                () -> assertNotNull(businessResponse.getBody().getStatus()),
                () -> assertNotNull(businessResponse.getBody().getMessage()),
                () -> assertNotNull(businessResponse.getBody().getPath()),
                () -> assertNotNull(businessResponse.getBody().getTimestamp()),
                
                () -> assertNotNull(insuranceResponse.getBody().getStatus()),
                () -> assertNotNull(insuranceResponse.getBody().getMessage()),
                () -> assertNotNull(insuranceResponse.getBody().getPath()),
                () -> assertNotNull(insuranceResponse.getBody().getTimestamp()),
                
                () -> assertNotNull(genericResponse.getBody().getStatus()),
                () -> assertNotNull(genericResponse.getBody().getMessage()),
                () -> assertNotNull(genericResponse.getBody().getPath()),
                () -> assertNotNull(genericResponse.getBody().getTimestamp())
        );
    }

    @Test
    @DisplayName("Should map exception types to correct HTTP status codes")
    void shouldMapExceptionTypesToCorrectHttpStatusCodes() {
        // Given
        ResourceNotFoundException notFoundEx = new ResourceNotFoundException("Resource", 1L);
        ValidationException validationEx = new ValidationException("Validation error");
        BusinessRuleException businessEx = new BusinessRuleException("Business rule error");
        InsuranceException insuranceEx = new InsuranceException("Insurance error");
        Exception genericEx = new RuntimeException("Generic error");
        
        // When
        ResponseEntity<ErrorResponse> notFoundResponse = exceptionHandler.handleResourceNotFound(notFoundEx, webRequest);
        ResponseEntity<ErrorResponse> validationResponse = exceptionHandler.handleValidationException(validationEx, webRequest);
        ResponseEntity<ErrorResponse> businessResponse = exceptionHandler.handleBusinessRuleException(businessEx, webRequest);
        ResponseEntity<ErrorResponse> insuranceResponse = exceptionHandler.handleInsuranceException(insuranceEx, webRequest);
        ResponseEntity<ErrorResponse> genericResponse = exceptionHandler.handleGlobalException(genericEx, webRequest);
        
        // Then
        assertAll("HTTP status codes should match exception types",
                () -> assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode()),
                () -> assertEquals(404, notFoundResponse.getBody().getStatus()),
                
                () -> assertEquals(HttpStatus.BAD_REQUEST, validationResponse.getStatusCode()),
                () -> assertEquals(400, validationResponse.getBody().getStatus()),
                
                () -> assertEquals(HttpStatus.BAD_REQUEST, businessResponse.getStatusCode()),
                () -> assertEquals(400, businessResponse.getBody().getStatus()),
                
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, insuranceResponse.getStatusCode()),
                () -> assertEquals(500, insuranceResponse.getBody().getStatus()),
                
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, genericResponse.getStatusCode()),
                () -> assertEquals(500, genericResponse.getBody().getStatus())
        );
    }
}
