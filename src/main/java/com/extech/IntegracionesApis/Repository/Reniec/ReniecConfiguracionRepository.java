package com.extech.IntegracionesApis.Repository.Reniec;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReniecConfiguracionRepository extends JpaRepository<ApiExternaFuncion, Integer> {

    /**
     * Busca configuración activa de Reniec DNI
     */
    @Query("""
            SELECT c FROM ApiExternaFuncion c \
            JOIN c.apiServicesFuncion f \
            WHERE f.codigo = 'RENIEC_DNI' AND c.activo = true""")
    Optional<ApiExternaFuncion> findConfiguracionReniecActiva();

    /**
     * Busca configuración por ID de función
     */
    Optional<ApiExternaFuncion> findByFuncion_FuncionId(Integer funcionId);

    /**
     * Busca configuración por ID de función y estado
     */
    Optional<ApiExternaFuncion> findByFuncion_FuncionIdAndActivo(Integer funcionId, Boolean activo);
}
