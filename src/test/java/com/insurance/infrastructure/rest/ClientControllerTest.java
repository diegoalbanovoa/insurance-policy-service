package com.insurance.infrastructure.rest;

import com.insurance.application.dto.ClientCreateRequest;
import com.insurance.application.dto.ClientResponse;
import com.insurance.application.usecase.ClientService;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientController Tests")
public class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @InjectMocks
    private ClientController clientController;

    private ClientCreateRequest createRequest;
    private ClientResponse clientResponse;

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

        clientResponse = ClientResponse.builder()
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
    @DisplayName("Should create client successfully and return 201")
    @SuppressWarnings("null")
    void testCreateClientSuccess() {
        when(clientService.createClient(any(ClientCreateRequest.class)))
                .thenReturn(clientResponse);

        ResponseEntity<ClientResponse> response = clientController.createClient(createRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Juan", response.getBody().getNombres());
        verify(clientService, times(1)).createClient(any(ClientCreateRequest.class));
    }

    @Test
    @DisplayName("Should retrieve client by ID successfully")
    @SuppressWarnings("null")
    void testGetClientByIdSuccess() {
        when(clientService.getClientById(1L))
                .thenReturn(clientResponse);

        ResponseEntity<ClientResponse> response = clientController.getClientById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    @DisplayName("Should get all clients")
    @SuppressWarnings("null")
    void testGetAllClientsSuccess() {
        ClientResponse client2 = ClientResponse.builder()
                .id(2L)
                .nombres("María")
                .build();
        
        List<ClientResponse> clients = Arrays.asList(clientResponse, client2);
        when(clientService.getAllClients()).thenReturn(clients);

        ResponseEntity<List<ClientResponse>> response = clientController.getAllClients();

        assertNotNull(response);
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(clientService, times(1)).getAllClients();
    }

    @Test
    @DisplayName("Should update client successfully")
    @SuppressWarnings("null")
    void testUpdateClientSuccess() {
        ClientResponse updatedResponse = ClientResponse.builder()
                .id(1L)
                .nombres("Juan Updated").build();

        when(clientService.updateClient(eq(1L), any(ClientCreateRequest.class)))
                .thenReturn(updatedResponse);

        ResponseEntity<ClientResponse> response = clientController.updateClient(1L, createRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Juan Updated", response.getBody().getNombres());
    }

    @Test
    @DisplayName("Should delete client successfully")
    void testDeleteClientSuccess() {
        doNothing().when(clientService).deleteClient(1L);

        ResponseEntity<Void> response = clientController.deleteClient(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(clientService, times(1)).deleteClient(1L);
    }

    @Test
    @DisplayName("Should throw BusinessRuleException on duplicate")
    void testCreateClientDuplicate() {
        when(clientService.createClient(any(ClientCreateRequest.class)))
                .thenThrow(new BusinessRuleException("Duplicate"));

        assertThrows(BusinessRuleException.class,
                () -> clientController.createClient(createRequest));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException on not found")
    void testGetClientByIdNotFound() {
        when(clientService.getClientById(999L))
                .thenThrow(new ResourceNotFoundException("Cliente", 999L));

        assertThrows(ResourceNotFoundException.class,
                () -> clientController.getClientById(999L));
    }
}
