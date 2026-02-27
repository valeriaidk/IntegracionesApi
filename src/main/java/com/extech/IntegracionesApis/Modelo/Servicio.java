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

public class Servicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idservicio;
    
    @ManyToOne
    @JoinColumn(name = "idempresa")
    private Empresa empresa;
    
    private String nombre;          // "RENIEC", "SUNAT"
    private String descripcion;     // "Consulta DNI RENIEC"
    private String urlbase;         // "https://api.decolecta.com/v1"
    private String endpoint;        // "/reniec/dni", "/sunat/ruc"
    private String metodo;          // "GET", "POST"
    private Boolean activo;
    private String usuariocreacion;
    private LocalDateTime fechacreacion;
    private String usuariomodificacion;
    private LocalDateTime fechamodificacion;
}
