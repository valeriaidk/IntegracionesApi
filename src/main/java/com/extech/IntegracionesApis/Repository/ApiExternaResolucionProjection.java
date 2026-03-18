package com.extech.IntegracionesApis.Repository;

/**
 * Proyección para mapear el resultado del SP dbo.uspResolverApiExternaPorUsuarioYFuncion.
 * Evita el mapeo directo a entidad cuando los alias no coinciden con columnas.
 */
public interface ApiExternaResolucionProjection {

    Integer getUsuarioId();

    Integer getApiServicesFuncionId();
    String getNombreFuncionInterna();
    String getCodigoFuncionInterna();
    String getEndpointInterno();
    String getMetodoInterno();

    Integer getApiAsignacionId();

    Integer getApiExternaFuncionId();
    String getNombreFuncionExterna();
    String getCodigoFuncionExterna();
    String getEndpointExterno();
    String getMetodoExterno();
    String getToken();
    String getAutorizacion();
    String getRequest();
    String getResponse();
    Integer getTiempoConsulta();
    String getSegmentoTiempo();
}

