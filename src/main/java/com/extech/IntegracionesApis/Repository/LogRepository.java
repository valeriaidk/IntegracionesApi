package com.extech.IntegracionesApis.Repository;

import com.extech.IntegracionesApis.Domain.Model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
    Optional<Log> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}
