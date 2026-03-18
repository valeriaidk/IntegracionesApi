package com.extech.IntegracionesApis.Repository.General;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiExternaFuncionRepository extends JpaRepository<ApiExternaFuncion, Integer> {
    Optional<ApiExternaFuncion> findByNombreAndActivoTrue(String nombre);
    List<ApiExternaFuncion> findByActivoTrue();
    Optional<ApiExternaFuncion> findByCodigoAndActivoTrueAndEliminadoFalse(String codigo);
    Optional<ApiExternaFuncion> findByApiExternaFuncionIdAndActivoTrueAndEliminadoFalse(Integer apiExternaFuncionId);
}
