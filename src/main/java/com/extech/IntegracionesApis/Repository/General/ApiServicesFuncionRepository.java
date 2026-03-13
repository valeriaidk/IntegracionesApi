package com.extech.IntegracionesApis.Repository.General;

import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiServicesFuncionRepository extends JpaRepository<ApiServicesFuncion, Integer> {
    Optional<ApiServicesFuncion> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
