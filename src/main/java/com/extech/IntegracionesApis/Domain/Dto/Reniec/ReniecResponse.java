package com.extech.IntegracionesApis.Domain.Dto.Reniec;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReniecResponse {
    private String first_name;
    private String first_last_name;
    private String second_last_name;
    private String full_name;
    private String document_number;
    private String tipo; // Tipo de consulta (DNI o RUC)
    private int limiteConsultas; // Límite de consultas permitidas
    private String mensaje; // Mensaje informativo
    private String plan; // Plan de uso, por defecto "free"
}
