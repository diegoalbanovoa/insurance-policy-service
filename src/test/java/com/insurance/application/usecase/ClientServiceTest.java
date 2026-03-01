package com.insurance.application.usecase;

import com.insurance.application.dto.ClientCreateRequest;
import com.insurance.application.dto.ClientResponse;
import com.insurance.domain.model.Client;
import com.insurance.domain.repository.ClientRepository;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientService Tests")
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private ClientCreateRequest createRequest;
    private Client existingClient;

    @BeforeEach
    void setUp() {
        createRequest = ClientCreateRequest.builder()
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan")
                .apellidos("García")
                .email("juan@example.com")
                .telefono("3101234567")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();

        existingClient = Client.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan")
                .apellidos("García")
                .email("juan@example.com")
                .telefono("3101234567")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();
    }

    @Test
    @DisplayName("Should create client successfully when document and email are unique")
    @SuppressWarnings("null")
    void testCreateClientSuccess() {
        // Given
        when(clientRepository.findByTipoDocumentoAndNumeroDocumento(
                createRequest.getTipoDocumento(),
                createRequest.getNumeroDocumento()))
                .thenReturn(Optional.empty());
        when(clientRepository.findByEmail(createRequest.getEmail()))
                .thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class)))
                .thenReturn(existingClient);

        // When
        ClientResponse response = clientService.createClient(createRequest);

        // Then
        assertNotNull(response);
        assertEquals("Juan", response.getNombres());
        assertEquals("juan@example.com", response.getEmail());
        assertTrue(response.getFullName().contains("Juan"));
        
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when document already exists")
    @SuppressWarnings("null")
    void testCreateClientDuplicateDocument() {
        // Given
        when(clientRepository.findByTipoDocumentoAndNumeroDocumento(
                createRequest.getTipoDocumento(),
                createRequest.getNumeroDocumento()))
                .thenReturn(Optional.of(existingClient));

        // When & Then
        assertThrows(BusinessRuleException.class,
                () -> clientService.createClient(createRequest),
                "Should throw BusinessRuleException for duplicate document");

        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when email already exists")
    @SuppressWarnings("null")
    void testCreateClientDuplicateEmail() {
        // Given
        when(clientRepository.findByTipoDocumentoAndNumeroDocumento(
                createRequest.getTipoDocumento(),
                createRequest.getNumeroDocumento()))
                .thenReturn(Optional.empty());
        when(clientRepository.findByEmail(createRequest.getEmail()))
                .thenReturn(Optional.of(existingClient));

        // When & Then
        assertThrows(BusinessRuleException.class,
                () -> clientService.createClient(createRequest),
                "Should throw BusinessRuleException for duplicate email");

        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should retrieve client by ID successfully")
    void testGetClientByIdSuccess() {
        // Given
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(existingClient));

        // When
        ClientResponse response = clientService.getClientById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("juan@example.com", response.getEmail());
        
        verify(clientRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when client ID not found")
    void testGetClientByIdNotFound() {
        // Given
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clientService.getClientById(999L),
                "Should throw ResourceNotFoundException");

        verify(clientRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should retrieve all clients successfully")
    void testGetAllClientsSuccess() {
        // Given
        Client client2 = Client.builder()
                .id(2L)
                .tipoDocumento("CE")
                .numeroDocumento("9876543210")
                .nombres("María")
                .apellidos("López")
                .email("maria@example.com")
                .telefono("3109876543")
                .fechaNacimiento(LocalDate.of(1995, 5, 20))
                .build();

        List<Client> clients = Arrays.asList(existingClient, client2);
        when(clientRepository.findAll()).thenReturn(clients);

        // When
        List<ClientResponse> responses = clientService.getAllClients();

        // Then
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("Juan", responses.get(0).getNombres());
        assertEquals("María", responses.get(1).getNombres());
        
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should retrieve client by document successfully")
    void testGetClientByDocumentSuccess() {
        // Given
        when(clientRepository.findByTipoDocumentoAndNumeroDocumento(
                "CC", "1234567890"))
                .thenReturn(Optional.of(existingClient));

        // When
        ClientResponse response = clientService.getClientByDocument("CC", "1234567890");

        // Then
        assertNotNull(response);
        assertEquals("Juan", response.getNombres());
        
        verify(clientRepository, times(1))
                .findByTipoDocumentoAndNumeroDocumento("CC", "1234567890");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when document not found")
    void testGetClientByDocumentNotFound() {
        // Given
        when(clientRepository.findByTipoDocumentoAndNumeroDocumento(
                "CC", "0000000000"))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clientService.getClientByDocument("CC", "0000000000"));
    }

    @Test
    @DisplayName("Should update client successfully")
    @SuppressWarnings("null")
    void testUpdateClientSuccess() {
        // Given
        ClientCreateRequest updateRequest = ClientCreateRequest.builder()
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan Updated")
                .apellidos("García Updated")
                .email("juan.updated@example.com")
                .telefono("3105555555")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();

        Client updatedClient = Client.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan Updated")
                .apellidos("García Updated")
                .email("juan.updated@example.com")
                .telefono("3105555555")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(existingClient));
        when(clientRepository.findByEmail("juan.updated@example.com"))
                .thenReturn(Optional.empty());
        when(clientRepository.save(any(Client.class)))
                .thenReturn(updatedClient);

        // When
        ClientResponse response = clientService.updateClient(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("Juan Updated", response.getNombres());
        assertEquals("juan.updated@example.com", response.getEmail());
        
        verify(clientRepository, times(1)).findById(1L);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when updating non-existent client")
    @SuppressWarnings("null")
    void testUpdateClientNotFound() {
        // Given
        when(clientRepository.findById(999L))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clientService.updateClient(999L, createRequest));

        verify(clientRepository, times(1)).findById(999L);
        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when updating with duplicate email")
    @SuppressWarnings("null")
    void testUpdateClientDuplicateEmail() {
        // Given
        ClientCreateRequest updateRequest = ClientCreateRequest.builder()
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan")
                .apellidos("García")
                .email("other@example.com")
                .telefono("3101234567")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();

        Client otherClient = Client.builder()
                .id(2L)
                .email("other@example.com")
                .build();

        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(existingClient));
        when(clientRepository.findByEmail("other@example.com"))
                .thenReturn(Optional.of(otherClient));

        // When & Then
        assertThrows(BusinessRuleException.class,
                () -> clientService.updateClient(1L, updateRequest));

        verify(clientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete client successfully")
    void testDeleteClientSuccess() {
        // Given
        when(clientRepository.existsById(1L)).thenReturn(true);

        // When
        clientService.deleteClient(1L);

        // Then
        verify(clientRepository, times(1)).existsById(1L);
        verify(clientRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when deleting non-existent client")
    @SuppressWarnings("null")
    void testDeleteClientNotFound() {
        // Given
        when(clientRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> clientService.deleteClient(999L));

        verify(clientRepository, times(1)).existsById(999L);
        verify(clientRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should map Client to ClientResponse correctly")
    void testClientMapping() {
        // Given
        when(clientRepository.findById(1L))
                .thenReturn(Optional.of(existingClient));

        // When
        ClientResponse response = clientService.getClientById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("CC", response.getTipoDocumento());
        assertEquals("1234567890", response.getNumeroDocumento());
        assertEquals("Juan", response.getNombres());
        assertEquals("García", response.getApellidos());
        assertEquals("juan@example.com", response.getEmail());
        assertEquals("3101234567", response.getTelefono());
        assertTrue(response.getFullName().contains("Juan"));
        assertTrue(response.getFullName().contains("García"));
    }
}
