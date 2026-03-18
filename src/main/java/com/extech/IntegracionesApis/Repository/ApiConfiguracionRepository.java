package com.extech.IntegracionesApis.Repository;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Repository.ApiExternaResolucionProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiConfiguracionRepository extends JpaRepository<ApiExternaFuncion, Integer> {

    /**
     * Ejecuta el stored procedure dbo.uspResolverApiExternaPorUsuarioYFuncion
     * para obtener la configuración completa del proveedor externo
     * 
     * @param usuarioId ID del usuario autenticado
     * @param codigoFuncion Código de la función interna (ej: SMS_SEND, RENIEC_DNI, SUNAT_RUC)
     * @return Configuración completa del proveedor externo o empty si no encuentra
     */
    @Query(value = """
        EXEC dbo.uspResolverApiExternaPorUsuarioYFuncion ?1, ?2
        """, nativeQuery = true)
    Optional<ApiExternaResolucionProjection> obtenerConfiguracionPorUsuarioYFuncion(
            Integer usuarioId,
            String codigoFuncion
    );
}
