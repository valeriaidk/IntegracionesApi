package com.extech.IntegracionesApis.Repository.Sunat;

import com.extech.IntegracionesApis.Domain.Model.ConfiguracionApiFuncion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SunatConfiguracionRepository extends JpaRepository<ConfiguracionApiFuncion, Integer> {

    /**
     * Busca configuración activa de Sunat por código de función
     */
    @Query("SELECT c FROM ConfiguracionApiFuncion c " +
           "JOIN c.funcion f " +
           "WHERE f.codigo = 'SUNAT_RUC' AND c.activo = true")
    Optional<ConfiguracionApiFuncion> findConfiguracionSunatActiva();

    /**
     * Busca configuración por ID de función
     */
    Optional<ConfiguracionApiFuncion> findByFuncion_FuncionId(Integer funcionId);

    /**
     * Busca configuración por ID de función y estado activo
     */
    Optional<ConfiguracionApiFuncion> findByFuncion_FuncionIdAndActivo(Integer funcionId, Boolean activo);
}
