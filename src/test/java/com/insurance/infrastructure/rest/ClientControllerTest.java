package com.insurance.infrastructure.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurance.application.dto.ClientCreateRequest;
import com.insurance.application.dto.ClientResponse;
import com.insurance.application.usecase.ClientService;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@DisplayName("ClientController Tests")
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

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
    void testCreateClientSuccess() throws Exception {
        // Given
        when(clientService.createClient(any(ClientCreateRequest.class)))
                .thenReturn(clientResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan@example.com"))
                .andExpect(jsonPath("$.nombres").value("Juan"));

        verify(clientService, times(1)).createClient(any(ClientCreateRequest.class));
    }

    @Test
    @DisplayName("Should return 409 when creating client with duplicate email")
    void testCreateClientDuplicate() throws Exception {
        // Given
        when(clientService.createClient(any(ClientCreateRequest.class)))
                .thenThrow(new BusinessRuleException("Email already exists"));

        // When & Then
        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Violación de regla de negocio"));

        verify(clientService, times(1)).createClient(any(ClientCreateRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when creating client with invalid data")
    void testCreateClientBadRequest() throws Exception {
        // Given
        ClientCreateRequest invalidRequest = ClientCreateRequest.builder()
                .tipoDocumento("")  // Invalid: empty
                .numeroDocumento("1234567890")
                .nombres("Juan")
                .apellidos("García")
                .email("invalid-email")  // Invalid email format
                .telefono("3101234567")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(clientService, never()).createClient(any());
    }

    @Test
    @DisplayName("Should retrieve client by ID successfully")
    void testGetClientByIdSuccess() throws Exception {
        // Given
        when(clientService.getClientById(1L))
                .thenReturn(clientResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/clients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("juan@example.com"))
                .andExpect(jsonPath("$.nombres").value("Juan"));

        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    @DisplayName("Should return 404 when getting non-existent client")
    void testGetClientByIdNotFound() throws Exception {
        // Given
        when(clientService.getClientById(999L))
                .thenThrow(new ResourceNotFoundException("Cliente", 999L));

        // When & Then
        mockMvc.perform(get("/api/v1/clients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Recurso no encontrado"));

        verify(clientService, times(1)).getClientById(999L);
    }

    @Test
    @DisplayName("Should retrieve all clients")
    void testGetAllClientsSuccess() throws Exception {
        // Given
        ClientResponse client2 = ClientResponse.builder()
                .id(2L)
                .tipoDocumento("CE")
                .numeroDocumento("9876543210")
                .nombres("María")
                .apellidos("López")
                .email("maria@example.com")
                .telefono("3109876543")
                .fechaNacimiento(LocalDate.of(1995, 5, 20))
                .build();

        List<ClientResponse> clients = Arrays.asList(clientResponse, client2);
        when(clientService.getAllClients()).thenReturn(clients);

        // When & Then
        mockMvc.perform(get("/api/v1/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nombres").value("Juan"))
                .andExpect(jsonPath("$[1].nombres").value("María"));

        verify(clientService, times(1)).getAllClients();
    }

    @Test
    @DisplayName("Should retrieve client by document")
    void testGetClientByDocumentSuccess() throws Exception {
        // Given
        when(clientService.getClientByDocument("CC", "1234567890"))
                .thenReturn(clientResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/clients/document/CC/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoDocumento").value("CC"))
                .andExpect(jsonPath("$.numeroDocumento").value("1234567890"));

        verify(clientService, times(1)).getClientByDocument("CC", "1234567890");
    }

    @Test
    @DisplayName("Should return 404 when document not found")
    void testGetClientByDocumentNotFound() throws Exception {
        // Given
        when(clientService.getClientByDocument("CC", "0000000000"))
                .thenThrow(new ResourceNotFoundException("Cliente"));

        // When & Then
        mockMvc.perform(get("/api/v1/clients/document/CC/0000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(clientService, times(1)).getClientByDocument("CC", "0000000000");
    }

    @Test
    @DisplayName("Should update client successfully")
    void testUpdateClientSuccess() throws Exception {
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

        ClientResponse updatedResponse = ClientResponse.builder()
                .id(1L)
                .tipoDocumento("CC")
                .numeroDocumento("1234567890")
                .nombres("Juan Updated")
                .apellidos("García Updated")
                .email("juan.updated@example.com")
                .telefono("3105555555")
                .fechaNacimiento(LocalDate.of(1990, 1, 15))
                .build();

        when(clientService.updateClient(1L, updateRequest))
                .thenReturn(updatedResponse);

        // When & Then
        mockMvc.perform(put("/api/v1/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("Juan Updated"))
                .andExpect(jsonPath("$.email").value("juan.updated@example.com"));

        verify(clientService, times(1)).updateClient(eq(1L), any(ClientCreateRequest.class));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent client")
    void testUpdateClientNotFound() throws Exception {
        // Given
        when(clientService.updateClient(eq(999L), any(ClientCreateRequest.class)))
                .thenThrow(new ResourceNotFoundException("Cliente", 999L));

        // When & Then
        mockMvc.perform(put("/api/v1/clients/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(clientService, times(1)).updateClient(eq(999L), any(ClientCreateRequest.class));
    }

    @Test
    @DisplayName("Should delete client successfully and return 204")
    void testDeleteClientSuccess() throws Exception {
        // Given
        doNothing().when(clientService).deleteClient(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/clients/1"))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient(1L);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent client")
    void testDeleteClientNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Cliente", 999L))
                .when(clientService).deleteClient(999L);

        // When & Then
        mockMvc.perform(delete("/api/v1/clients/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(clientService, times(1)).deleteClient(999L);
    }

    @Test
    @DisplayName("Should return 400 for missing required fields")
    void testCreateClientMissingFields() throws Exception {
        // Given
        String invalidJson = "{ \"tipoDocumento\": \"CC\" }";

        // When & Then
        mockMvc.perform(post("/api/v1/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Validación")));

        verify(clientService, never()).createClient(any());
    }
}
