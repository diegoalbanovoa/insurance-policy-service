package com.insurance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Client entity class.
 */
@DisplayName("Client Entity Tests")
class ClientTest {

    @Test
    @DisplayName("Should create client using builder")
    void shouldCreateClientUsingBuilder() {
        // Given
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        
        // When
        Client client = Client.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .nombres("Juan")
                .apellidos("Pérez García")
                .email("juan.perez@example.com")
                .telefono("3001234567")
                .fechaNacimiento(birthDate)
                .build();
        
        // Then
        assertNotNull(client);
        assertEquals(1L, client.getId());
        assertEquals("CC", client.getTipoDocumento());
        assertEquals("12345678", client.getNumeroDocumento());
        assertEquals("Juan", client.getNombres());
        assertEquals("Pérez García", client.getApellidos());
        assertEquals("juan.perez@example.com", client.getEmail());
        assertEquals("3001234567", client.getTelefono());
        assertEquals(birthDate, client.getFechaNacimiento());
    }

    @Test
    @DisplayName("Should create client using no-args constructor")
    void shouldCreateClientUsingNoArgsConstructor() {
        // When
        Client client = new Client();
        
        // Then
        assertNotNull(client);
        assertNull(client.getId());
        assertNull(client.getTipoDocumento());
        assertNull(client.getNumeroDocumento());
        assertNull(client.getNombres());
        assertNull(client.getApellidos());
    }

    @Test
    @DisplayName("Should return full name correctly")
    void shouldReturnFullNameCorrectly() {
        // Given
        Client client = Client.builder()
                .nombres("María José")
                .apellidos("López Rodríguez")
                .build();
        
        // When
        String fullName = client.getFullName();
        
        // Then
        assertEquals("María José López Rodríguez", fullName);
    }

    @Test
    @DisplayName("Should handle full name with single first name and last name")
    void shouldHandleFullNameWithSingleFirstNameAndLastName() {
        // Given
        Client client = Client.builder()
                .nombres("Carlos")
                .apellidos("González")
                .build();
        
        // When
        String fullName = client.getFullName();
        
        // Then
        assertEquals("Carlos González", fullName);
    }

    @Test
    @DisplayName("Should call onCreate when PrePersist is triggered")
    void shouldCallOnCreateWhenPrePersistIsTriggered() throws Exception {
        // Given
        Client client = new Client();
        Method onCreateMethod = Client.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        
        // When
        onCreateMethod.invoke(client);
        
        // Then
        assertNotNull(client.getCreatedAt());
        assertNotNull(client.getUpdatedAt());
        assertEquals(client.getCreatedAt(), client.getUpdatedAt());
    }

    @Test
    @DisplayName("Should call onUpdate when PreUpdate is triggered")
    void shouldCallOnUpdateWhenPreUpdateIsTriggered() throws Exception {
        // Given
        Client client = new Client();
        Method onCreateMethod = Client.class.getDeclaredMethod("onCreate");
        onCreateMethod.setAccessible(true);
        onCreateMethod.invoke(client);
        
        LocalDateTime originalCreatedAt = client.getCreatedAt();
        LocalDateTime originalUpdatedAt = client.getUpdatedAt();
        
        Thread.sleep(10); // Small delay to ensure different timestamp
        
        Method onUpdateMethod = Client.class.getDeclaredMethod("onUpdate");
        onUpdateMethod.setAccessible(true);
        
        // When
        onUpdateMethod.invoke(client);
        
        // Then
        assertEquals(originalCreatedAt, client.getCreatedAt()); // createdAt should not change
        assertNotEquals(originalUpdatedAt, client.getUpdatedAt()); // updatedAt should change
        assertTrue(client.getUpdatedAt().isAfter(originalUpdatedAt));
    }

    @Test
    @DisplayName("Should support equals and hashCode")
    void shouldSupportEqualsAndHashCode() {
        // Given
        LocalDate birthDate = LocalDate.of(1990, 5, 15);
        
        Client client1 = Client.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan@example.com")
                .telefono("3001234567")
                .fechaNacimiento(birthDate)
                .build();
        
        Client client2 = Client.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("12345678")
                .nombres("Juan")
                .apellidos("Pérez")
                .email("juan@example.com")
                .telefono("3001234567")
                .fechaNacimiento(birthDate)
                .build();
        
        // Then
        assertEquals(client1, client2);
        assertEquals(client1.hashCode(), client2.hashCode());
    }

    @Test
    @DisplayName("Should support toString method")
    void shouldSupportToStringMethod() {
        // Given
        Client client = Client.builder()
                .tipoDocumento("CC")
                .numeroDocumento("87654321")
                .nombres("Ana")
                .apellidos("Martínez")
                .email("ana@example.com")
                .build();
        
        // When
        String toString = client.toString();
        
        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("Ana"));
        assertTrue(toString.contains("Martínez"));
        assertTrue(toString.contains("ana@example.com"));
    }

    @Test
    @DisplayName("Should set all properties using setters")
    void shouldSetAllPropertiesUsingSetters() {
        // Given
        Client client = new Client();
        LocalDate birthDate = LocalDate.of(1985, 12, 20);
        
        // When
        client.setId(100L);
        client.setTipoDocumento("TI");
        client.setNumeroDocumento("98765432");
        client.setNombres("Pedro");
        client.setApellidos("Sánchez");
        client.setEmail("pedro@example.com");
        client.setTelefono("3109876543");
        client.setFechaNacimiento(birthDate);
        
        // Then
        assertEquals(100L, client.getId());
        assertEquals("TI", client.getTipoDocumento());
        assertEquals("98765432", client.getNumeroDocumento());
        assertEquals("Pedro", client.getNombres());
        assertEquals("Sánchez", client.getApellidos());
        assertEquals("pedro@example.com", client.getEmail());
        assertEquals("3109876543", client.getTelefono());
        assertEquals(birthDate, client.getFechaNacimiento());
    }

    @Test
    @DisplayName("Should handle different document types")
    void shouldHandleDifferentDocumentTypes() {
        // Given
        String[] documentTypes = {"CC", "TI", "CE", "PA", "NIT"};
        
        // When & Then
        for (String docType : documentTypes) {
            Client client = Client.builder()
                    .tipoDocumento(docType)
                    .numeroDocumento("123456")
                    .nombres("Test")
                    .apellidos("User")
                    .email("test@example.com")
                    .telefono("300000000")
                    .fechaNacimiento(LocalDate.of(1990, 1, 1))
                    .build();
            
            assertEquals(docType, client.getTipoDocumento());
        }
    }

    @Test
    @DisplayName("Should handle email validation format")
    void shouldHandleEmailValidationFormat() {
        // Given
        String[] emails = {
            "user@example.com",
            "test.user@company.co",
            "admin@subdomain.example.com"
        };
        
        // When & Then
        for (String email : emails) {
            Client client = Client.builder()
                    .tipoDocumento("CC")
                    .numeroDocumento("123456")
                    .nombres("Test")
                    .apellidos("User")
                    .email(email)
                    .telefono("300000000")
                    .fechaNacimiento(LocalDate.of(1990, 1, 1))
                    .build();
            
            assertEquals(email, client.getEmail());
        }
    }

    @Test
    @DisplayName("Should handle Colombian phone number format")
    void shouldHandleColombianPhoneNumberFormat() {
        // Given
        String[] phoneNumbers = {"3001234567", "3109876543", "3151112233"};
        
        // When & Then
        for (String phone : phoneNumbers) {
            Client client = Client.builder()
                    .tipoDocumento("CC")
                    .numeroDocumento("123456")
                    .nombres("Test")
                    .apellidos("User")
                    .email("test@example.com")
                    .telefono(phone)
                    .fechaNacimiento(LocalDate.of(1990, 1, 1))
                    .build();
            
            assertEquals(phone, client.getTelefono());
        }
    }

    @Test
    @DisplayName("Should create client with minimum required fields")
    void shouldCreateClientWithMinimumRequiredFields() {
        // Given
        LocalDate birthDate = LocalDate.of(1995, 8, 10);
        
        // When
        Client client = Client.builder()
                .tipoDocumento("CC")
                .numeroDocumento("11111111")
                .nombres("Mínimo")
                .apellidos("Requerido")
                .email("minimo@example.com")
                .telefono("3000000000")
                .fechaNacimiento(birthDate)
                .build();
        
        // Then
        assertNotNull(client);
        assertNull(client.getId()); // ID is auto-generated
        assertEquals("CC", client.getTipoDocumento());
        assertEquals("11111111", client.getNumeroDocumento());
        assertEquals("Mínimo", client.getNombres());
        assertEquals("Requerido", client.getApellidos());
        assertEquals("minimo@example.com", client.getEmail());
        assertEquals("3000000000", client.getTelefono());
        assertEquals(birthDate, client.getFechaNacimiento());
    }

    @Test
    @DisplayName("Should handle names with special characters")
    void shouldHandleNamesWithSpecialCharacters() {
        // When
        Client client = Client.builder()
                .nombres("María José")
                .apellidos("García-Pérez López")
                .email("test@example.com")
                .build();
        
        // Then
        assertEquals("María José", client.getNombres());
        assertEquals("García-Pérez López", client.getApellidos());
        assertEquals("María José García-Pérez López", client.getFullName());
    }
}
