package com.extech.IntegracionesApis.Repository.Sunat;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SunatConfiguracionRepository extends JpaRepository<ApiExternaFuncion, Integer> {

    /**
     * Busca configuración activa de Sunat por código de función
     */
    @Query("SELECT c FROM ApiExternaFuncion c " +
           "JOIN c.apiServicesFuncion f " +
           "WHERE f.codigo = 'SUNAT_RUC' AND c.activo = true")
    Optional<ApiExternaFuncion> findConfiguracionSunatActiva();

    /**
     * Busca configuración por ID de función
     */
    Optional<ApiExternaFuncion> findByFuncion_FuncionId(Integer funcionId);

    /**
     * Busca configuración por ID de función y estado activo
     */
    Optional<ApiExternaFuncion> findByFuncion_FuncionIdAndActivo(Integer funcionId, Boolean activo);
}
