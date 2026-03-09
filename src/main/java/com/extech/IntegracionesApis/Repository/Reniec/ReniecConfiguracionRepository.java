package com.extech.IntegracionesApis.Repository.Reniec;

import com.extech.IntegracionesApis.Domain.Model.ConfiguracionApiFuncion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReniecConfiguracionRepository extends JpaRepository<ConfiguracionApiFuncion, Integer> {

    /**
     * Busca configuración activa de Reniec DNI
     */
    @Query("SELECT c FROM ConfiguracionApiFuncion c " +
           "JOIN c.funcion f " +
           "WHERE f.codigo = 'RENIEC_DNI' AND c.activo = true")
    Optional<ConfiguracionApiFuncion> findConfiguracionReniecActiva();

    /**
     * Busca configuración por ID de función
     */
    Optional<ConfiguracionApiFuncion> findByFuncion_FuncionId(Integer funcionId);

    /**
     * Busca configuración por ID de función y estado
     */
    Optional<ConfiguracionApiFuncion> findByFuncion_FuncionIdAndActivo(Integer funcionId, Boolean activo);
}
