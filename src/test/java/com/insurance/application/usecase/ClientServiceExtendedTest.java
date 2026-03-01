package com.insurance.application.usecase;

import com.insurance.domain.model.Client;
import com.insurance.domain.repository.ClientRepository;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import com.insurance.shared.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService Extended Tests")
class ClientServiceExtendedTest {

    @Mock
    private ClientRepository clientRepository;

    private ClientService clientService;

    private Client testClient;

    @BeforeEach
    void setUp() {
        clientService = new ClientService(clientRepository);

        testClient = Client.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan")
                .apellidos("Pérez García")
                .email("juan.perez@example.com")
                .telefono("+57 3001234567")
                .fechaNacimiento(LocalDate.of(1990, 5, 15))
                .build();
    }

    @Test
    @DisplayName("Should throw ValidationException for null nombres")
    void testCreateClient_NullNombres() {
        // Arrange
        testClient.setNombres(null);

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            clientService.createClient(testClient);
        });
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException for empty email")
    void testCreateClient_EmptyEmail() {
        // Arrange
        testClient.setEmail("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            clientService.createClient(testClient);
        });
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ValidationException for future birth date")
    void testCreateClient_FutureBirthDate() {
        // Arrange
        testClient.setFechaNacimiento(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            clientService.createClient(testClient);
        });
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw resourceNotFoundException when deleting non-existent client")
    void testDeleteClient_NotFound() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.deleteClient(999L);
        });
        verify(clientRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should list all clients successfully")
    void testListAllClients() {
        // Arrange
        Client client2 = Client.builder()
                .id(2L)
                .tipoDocumento("CC")
                .numeroDocumento("9876543210")
                .nombres("María")
                .apellidos("López García")
                .email("maria.lopez@example.com")
                .telefono("+57 3009876543")
                .fechaNacimiento(LocalDate.of(1992, 8, 20))
                .build();

        when(clientRepository.findAll()).thenReturn(Arrays.asList(testClient, client2));

        // Act
        var clients = clientService.listAllClients();

        // Assert
        assertEquals(2, clients.size());
        assertEquals("juan.perez@example.com", clients.get(0).getEmail());
        assertEquals("maria.lopez@example.com", clients.get(1).getEmail());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no clients exist")
    void testListAllClients_Empty() {
        // Arrange
        when(clientRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        var clients = clientService.listAllClients();

        // Assert
        assertTrue(clients.isEmpty());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should update client successfully")
    void testUpdateClient_Success() {
        // Arrange
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);

        testClient.setNombres("Juan Carlos");
        testClient.setTelefono("+57 3119876543");

        // Act
        Client updatedClient = clientService.updateClient(1L, testClient);

        // Assert
        assertNotNull(updatedClient);
        assertEquals("Juan Carlos", updatedClient.getNombres());
        assertEquals("+57 3119876543", updatedClient.getTelefono());
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent client")
    void testUpdateClient_NotFound() {
        // Arrange
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            clientService.updateClient(999L, testClient);
        });
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle duplicate email on update")
    void testUpdateClient_DuplicateEmail() {
        // Arrange
        Client existingClient = Client.builder()
                .id(2L)
                .email("other@example.com")
                .build();

        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.findByEmailIgnoreCase("other@example.com"))
                .thenReturn(Optional.of(existingClient));

        testClient.setEmail("other@example.com");

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> {
            clientService.updateClient(1L, testClient);
        });
        verify(clientRepository, never()).save(any());
    }
}
