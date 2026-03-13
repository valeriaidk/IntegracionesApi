package com.extech.IntegracionesApis.Repository.Sunat;

import com.extech.IntegracionesApis.Domain.Model.ApiExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SunatConfiguracionRepository extends JpaRepository<ApiExterna, Integer> {

    /**
     * Busca configuración activa de Sunat por código de función
     */
    @Query("SELECT c FROM ApiExterna c " +
           "JOIN c.funcion f " +
           "WHERE f.codigo = 'SUNAT_RUC' AND c.activo = true")
    Optional<ApiExterna> findConfiguracionSunatActiva();

    /**
     * Busca configuración por ID de función
     */
    Optional<ApiExterna> findByFuncion_FuncionId(Integer funcionId);

    /**
     * Busca configuración por ID de función y estado activo
     */
    Optional<ApiExterna> findByFuncion_FuncionIdAndActivo(Integer funcionId, Boolean activo);
}
