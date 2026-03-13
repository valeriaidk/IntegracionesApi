package com.extech.IntegracionesApis.Repository.Reniec;

import com.extech.IntegracionesApis.Domain.Model.ApiExterna;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReniecConfiguracionRepository extends JpaRepository<ApiExterna, Integer> {

    /**
     * Busca configuración activa de Reniec DNI
     */
    @Query("SELECT c FROM ApiExterna c " +
           "JOIN c.funcion f " +
           "WHERE f.codigo = 'RENIEC_DNI' AND c.activo = true")
    Optional<ApiExterna> findConfiguracionReniecActiva();

    /**
     * Busca configuración por ID de función
     */
    Optional<ApiExterna> findByFuncion_FuncionId(Integer funcionId);

    /**
     * Busca configuración por ID de función y estado
     */
    Optional<ApiExterna> findByFuncion_FuncionIdAndActivo(Integer funcionId, Boolean activo);
}
