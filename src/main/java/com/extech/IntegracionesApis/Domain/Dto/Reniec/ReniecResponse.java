package com.extech.IntegracionesApis.Domain.Dto.Reniec;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReniecResponse {
    private String numeroDocumento;
    private String nombres;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private String nombreCompleto;
    private String direccion;
    private String ubigeo;
    private String razonSocial; // Para RUC
}
