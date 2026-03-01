package com.insurance.application.usecase;

import com.insurance.application.dto.ClientCreateRequest;
import com.insurance.application.dto.ClientResponse;
import com.insurance.domain.model.Client;
import com.insurance.domain.repository.ClientRepository;
import com.insurance.shared.exception.BusinessRuleException;
import com.insurance.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepository clientRepository;

    /**
     * Crea un nuevo cliente con los datos proporcionados.
     *
     * @param request datos del cliente a crear
     * @return DTO del cliente creado
     * @throws BusinessRuleException si el cliente ya existe (documento duplicado o email duplicado)
     */
    public ClientResponse createClient(ClientCreateRequest request) {
        // Validar que no exista cliente con el mismo documento
        clientRepository.findByTipoDocumentoAndNumeroDocumento(
                request.getTipoDocumento(), 
                request.getNumeroDocumento()
        ).ifPresent(c -> {
            throw new BusinessRuleException(
                "Ya existe un cliente registrado con el documento " + 
                request.getTipoDocumento() + " " + request.getNumeroDocumento()
            );
        });

        // Validar que no exista cliente con el mismo email
        clientRepository.findByEmail(request.getEmail()).ifPresent(c -> {
            throw new BusinessRuleException("El email " + request.getEmail() + " ya está registrado");
        });

        // Crear y guardar el cliente
        Client client = Client.builder()
                .tipoDocumento(request.getTipoDocumento())
                .numeroDocumento(request.getNumeroDocumento())
                .nombres(request.getNombres())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .build();

        Client savedClient = clientRepository.save(client);
        return mapToResponse(savedClient);
    }

    /**
     * Obtiene un cliente por su ID.
     *
     * @param id identificador del cliente
     * @return DTO del cliente
     * @throws ResourceNotFoundException si el cliente no existe
     */
    @Transactional(readOnly = true)
    public ClientResponse getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
        return mapToResponse(client);
    }

    /**
     * Obtiene todos los clientes registrados.
     *
     * @return lista de DTOs de clientes
     */
    @Transactional(readOnly = true)
    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Busca clientes por documento.
     *
     * @param tipoDocumento tipo de documento
     * @param numeroDocumento número de documento
     * @return DTO del cliente si existe
     * @throws ResourceNotFoundException si no existe cliente con ese documento
     */
    @Transactional(readOnly = true)
    public ClientResponse getClientByDocument(String tipoDocumento, String numeroDocumento) {
        Client client = clientRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Cliente con documento " + tipoDocumento + " " + numeroDocumento + " no encontrado"
                ));
        return mapToResponse(client);
    }

    /**
     * Actualiza los datos de un cliente.
     *
     * @param id identificador del cliente
     * @param request nuevos datos del cliente
     * @return DTO del cliente actualizado
     * @throws ResourceNotFoundException si el cliente no existe
     * @throws BusinessRuleException si los nuevos datos violarían restricciones únicas
     */
    public ClientResponse updateClient(Long id, ClientCreateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));

        // Validar email si cambió
        if (!client.getEmail().equals(request.getEmail())) {
            clientRepository.findByEmail(request.getEmail()).ifPresent(c -> {
                throw new BusinessRuleException("El email " + request.getEmail() + " ya está registrado");
            });
        }

        // Actualizar datos
        client.setNombres(request.getNombres());
        client.setApellidos(request.getApellidos());
        client.setEmail(request.getEmail());
        client.setTelefono(request.getTelefono());
        client.setFechaNacimiento(request.getFechaNacimiento());

        Client updatedClient = clientRepository.save(client);
        return mapToResponse(updatedClient);
    }

    /**
     * Elimina un cliente por su ID.
     *
     * @param id identificador del cliente
     * @throws ResourceNotFoundException si el cliente no existe
     */
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente", id);
        }
        clientRepository.deleteById(id);
    }

    /**
     * Mapea una entidad Client a su DTO correspondiente.
     *
     * @param client entidad del cliente
     * @return DTO del cliente
     */
    private ClientResponse mapToResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .tipoDocumento(client.getTipoDocumento())
                .numeroDocumento(client.getNumeroDocumento())
                .nombres(client.getNombres())
                .apellidos(client.getApellidos())
                .fullName(client.getFullName())
                .email(client.getEmail())
                .telefono(client.getTelefono())
                .fechaNacimiento(client.getFechaNacimiento())
                .build();
    }
}
