package com.extech.IntegracionesApis.Repository.General;

import com.extech.IntegracionesApis.Domain.Model.ApiFuncion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiFuncionRepository extends JpaRepository<ApiFuncion, Integer> {
    Optional<ApiFuncion> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
