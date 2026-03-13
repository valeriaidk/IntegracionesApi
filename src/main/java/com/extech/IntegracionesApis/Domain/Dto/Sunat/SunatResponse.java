package com.extech.IntegracionesApis.Domain.Dto.Sunat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SunatResponse {
    private String razon_social;
    private String numero_documento;
    private String estado;
    private String condicion;
    private String direccion;
    private String ubigeo;
    private String via_tipo;
    private String via_nombre;
    private String zona_codigo;
    private String zona_tipo;
    private String numero;
    private String interior;
    private String lote;
    private String dpto;
    private String manzana;
    private String kilometro;
    private String distrito;
    private String provincia;
    private String departamento;
    private Boolean es_agente_retencion;
    private Boolean es_buen_contribuyente;
    private Object[] locales_anexos;
    private String tipo;
    private String actividad_economica;
    private String numero_trabajadores;
    private String tipo_facturacion;
    private String tipo_contabilidad;
    private String comercio_exterior;
    private String tipoConsulta; // Tipo de consulta (DNI o RUC)
    private int limiteConsultas; // Límite de consultas permitidas
    private String mensaje; // Mensaje informativo
    private String plan; // Plan de uso, por defecto "free"
}
