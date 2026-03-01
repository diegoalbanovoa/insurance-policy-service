package com.insurance.infrastructure.rest;

import com.insurance.application.dto.ClientCreateRequest;
import com.insurance.application.dto.ClientResponse;
import com.insurance.application.usecase.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de clientes.
 * Expone endpoints para CRUD de clientes con validación completa.
 * Todos los endpoints requieren autenticación JWT.
 */
@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
@Tag(
    name = "Clients",
    description = "APIs para CRUD de clientes. Administra información personal, contacto y documentaria. " +
                 "Requiere autenticación con JWT token."
)
public class ClientController {

    private final ClientService clientService;

    /**
     * ENDPOINT: Crear nuevo cliente
     *
     * Registra un nuevo cliente validando que documento y email sean únicos.
     * El cliente recibe un ID que se usa para futuras operaciones.
     *
     * Validaciones: documento único, email válido y único, datos completos.
     *
     * @param request datos del cliente a crear
     * @return ResponseEntity con los datos del cliente creado (201 Created) incluyendo ID asignado
     */
    @PostMapping
    @Operation(
        summary = "Crear un nuevo cliente",
        description = "Registra un cliente validando documento y email únicos. " +
                     "Retorna el cliente con ID asignado e información de auditoría.",
        tags = {"Operaciones básicas"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Cliente creado exitosamente con ID asignado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ClientResponse.class))
        ),
        @ApiResponse(responseCode = "400", description = "Validación fallida: datos malformados o incompletos"),
        @ApiResponse(responseCode = "409", description = "Conflicto: documento o email ya existe en el sistema"),
        @ApiResponse(responseCode = "401", description = "No autorizado: token JWT inválido o expirado")
    })
    public ResponseEntity<ClientResponse> createClient(@Valid @RequestBody ClientCreateRequest request) {
        ClientResponse response = clientService.createClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Obtiene un cliente por su ID.
     *
     * @param id identificador del cliente
     * @return ResponseEntity con los datos del cliente
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID",
               description = "Recupera los datos de un cliente específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = ClientResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClientResponse> getClientById(
            @Parameter(description = "ID del cliente", example = "1")
            @PathVariable Long id) {
        ClientResponse response = clientService.getClientById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los clientes.
     *
     * @return ResponseEntity con la lista de clientes
     */
    @GetMapping
    @Operation(summary = "Listar todos los clientes",
               description = "Obtiene la lista completa de todos los clientes registrados")
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida",
                content = @Content(mediaType = "application/json",
                                 schema = @Schema(implementation = ClientResponse.class)))
    public ResponseEntity<List<ClientResponse>> getAllClients() {
        List<ClientResponse> response = clientService.getAllClients();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un cliente por su tipo y número de documento.
     *
     * @param tipoDocumento tipo de documento (ej: CC, CE, PP)
     * @param numeroDocumento número de documento
     * @return ResponseEntity con los datos del cliente
     */
    @GetMapping("/document/{tipoDocumento}/{numeroDocumento}")
    @Operation(summary = "Obtener cliente por documento",
               description = "Busca un cliente por su tipo y número de documento")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente encontrado",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = ClientResponse.class))),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<ClientResponse> getClientByDocument(
            @Parameter(description = "Tipo de documento", example = "CC") 
            @PathVariable String tipoDocumento,
            @Parameter(description = "Número de documento", example = "1234567")
            @PathVariable String numeroDocumento) {
        ClientResponse response = clientService.getClientByDocument(tipoDocumento, numeroDocumento);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza los datos de un cliente.
     *
     * @param id identificador del cliente
     * @param request datos actualizados del cliente
     * @return ResponseEntity con los datos del cliente actualizado
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente",
               description = "Actualiza los datos de un cliente existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cliente actualizado",
                    content = @Content(mediaType = "application/json",
                                     schema = @Schema(implementation = ClientResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validación fallida"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
        @ApiResponse(responseCode = "409", description = "Email ya está en uso por otro cliente")
    })
    public ResponseEntity<ClientResponse> updateClient(
            @Parameter(description = "ID del cliente", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ClientCreateRequest request) {
        ClientResponse response = clientService.updateClient(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina un cliente.
     *
     * @param id identificador del cliente
     * @return ResponseEntity sin contenido (204 No Content)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente",
               description = "Elimina un cliente del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Cliente eliminado"),
        @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> deleteClient(
            @Parameter(description = "ID del cliente", example = "1")
            @PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}
