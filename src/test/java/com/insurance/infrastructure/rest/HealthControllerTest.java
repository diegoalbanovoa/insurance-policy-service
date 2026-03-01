package com.insurance.infrastructure.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for HealthController class.
 */
@DisplayName("HealthController Tests")
class HealthControllerTest {

    private HealthController healthController;

    @BeforeEach
    void setUp() {
        healthController = new HealthController();
    }

    @Test
    @DisplayName("Should return health check response with status UP")
    @SuppressWarnings("null")
    void shouldReturnHealthCheckResponseWithStatusUp() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();
        
        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Should include application name in response")
    void shouldIncludeApplicationNameInResponse() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();
        
        // Then
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("application"));
        assertEquals("Insurance Policy Service", body.get("application"));
    }

    @Test
    @DisplayName("Should include timestamp in response")
    void shouldIncludeTimestampInResponse() {
        // Given
        long beforeCall = System.currentTimeMillis();
        
        // When
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();
        long afterCall = System.currentTimeMillis();
        
        // Then
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        
        Long timestamp = (Long) body.get("timestamp");
        assertNotNull(timestamp);
        assertTrue(timestamp >= beforeCall);
        assertTrue(timestamp <= afterCall);
    }

    @Test
    @DisplayName("Should return response with 3 fields")
    void shouldReturnResponseWithThreeFields() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();
        
        // Then
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(3, body.size());
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("application"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    @DisplayName("Should return OK status code")
    void shouldReturnOkStatusCode() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    @DisplayName("Should return consistent response on multiple calls")
    @SuppressWarnings("null")
    void shouldReturnConsistentResponseOnMultipleCalls() {
        // When
        ResponseEntity<Map<String, Object>> response1 = healthController.healthCheck();
        ResponseEntity<Map<String, Object>> response2 = healthController.healthCheck();
        
        // Then
        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody().get("status"), response2.getBody().get("status"));
        assertEquals(response1.getBody().get("application"), response2.getBody().get("application"));
    }

    @Test
    @DisplayName("Should have different timestamps on sequential calls")
    @SuppressWarnings("null")
    void shouldHaveDifferentTimestampsOnSequentialCalls() throws InterruptedException {
        // When
        ResponseEntity<Map<String, Object>> response1 = healthController.healthCheck();
        Thread.sleep(10); // Small delay to ensure different timestamps
        ResponseEntity<Map<String, Object>> response2 = healthController.healthCheck();
        
        // Then
        Long timestamp1 = (Long) response1.getBody().get("timestamp");
        Long timestamp2 = (Long) response2.getBody().get("timestamp");
        
        assertNotNull(timestamp1);
        assertNotNull(timestamp2);
        assertTrue(timestamp2 >= timestamp1);
    }

    @Test
    @DisplayName("Should return non-null response body")
    void shouldReturnNonNullResponseBody() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();
        
        // Then
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Should contain all expected keys in response")
    @SuppressWarnings("null")
    void shouldContainAllExpectedKeysInResponse() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();
        Map<String, Object> body = response.getBody();
        
        // Then
        assertAll("Response should contain all required fields",
            () -> assertTrue(body.containsKey("status"), "Should contain 'status' field"),
            () -> assertTrue(body.containsKey("application"), "Should contain 'application' field"),
            () -> assertTrue(body.containsKey("timestamp"), "Should contain 'timestamp' field")
        );
    }

    @Test
    @DisplayName("Should have correct value types in response")
    @SuppressWarnings("null")
    void shouldHaveCorrectValueTypesInResponse() {
        // When
        ResponseEntity<Map<String, Object>> response = healthController.healthCheck();
        Map<String, Object> body = response.getBody();
        
        // Then
        assertAll("Response values should have correct types",
            () -> assertTrue(body.get("status") instanceof String, "'status' should be String"),
            () -> assertTrue(body.get("application") instanceof String, "'application' should be String"),
            () -> assertTrue(body.get("timestamp") instanceof Long, "'timestamp' should be Long")
        );
    }
}
