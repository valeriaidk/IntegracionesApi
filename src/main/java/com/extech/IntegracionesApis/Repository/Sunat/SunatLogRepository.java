package com.extech.IntegracionesApis.Repository.Sunat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SunatLogRepository extends JpaRepository<Log, Integer> {
    Optional<Log> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}
