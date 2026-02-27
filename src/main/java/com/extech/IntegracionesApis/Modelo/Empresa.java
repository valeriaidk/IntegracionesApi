package com.extech.IntegracionesApis.Modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idempresa;
    private String codigo; // RUC o DNI de la empresa
    private String nombre;
    private String tipoDocumento; 
    private Boolean activo;
    private LocalDateTime fechacreacion;
}
