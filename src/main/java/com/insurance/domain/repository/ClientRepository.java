package com.insurance.domain.repository;

import com.insurance.domain.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
    Optional<Client> findByEmail(String email);
}
