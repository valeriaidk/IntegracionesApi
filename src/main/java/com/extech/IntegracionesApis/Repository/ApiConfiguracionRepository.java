package com.extech.IntegracionesApis.Repository;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiConfiguracionRepository extends JpaRepository<ApiExternaFuncion, Integer> {

    /**
     * Ejecuta el stored procedure uspObtenerConfiguracionApiExternaPorUsuario
     * para obtener la configuración completa del proveedor externo
     * 
     * @param usuarioId ID del usuario autenticado
     * @param codigoFuncion Código de la función interna (ej: SMS_SEND, RENIEC_DNI, SUNAT_RUC)
     * @return Configuración completa del proveedor externo o empty si no encuentra
     */
    @Query(value = """
        EXEC uspObtenerConfiguracionApiExternaPorUsuario 
        @UsuarioId = :usuarioId, 
        @CodigoFuncion = :codigoFuncion
        """, nativeQuery = true)
    Optional<ApiExternaFuncion> obtenerConfiguracionPorUsuarioYFuncion(
            @Param("usuarioId") Integer usuarioId, 
            @Param("codigoFuncion") String codigoFuncion
    );

    /**
     * Obtiene la función interna por su código
     * 
     * @param codigo Código de la función interna
     * @return Función interna o empty si no encuentra
     */
    Optional<ApiServicesFuncion> findByCodigoAndActivoTrue(String codigo);
}
