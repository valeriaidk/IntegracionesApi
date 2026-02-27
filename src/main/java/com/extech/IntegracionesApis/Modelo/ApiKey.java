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
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idapikey;
    
    @ManyToOne
    @JoinColumn(name = "idempresa")
    private Empresa empresa;
    
    @ManyToOne
    @JoinColumn(name = "idservicio")
    private Servicio servicio;
    
    private String apikey;          // Token completo
    private String prefijo;         // "Bearer ", "Basic "
    private String descripcion;     // "Token producción RENIEC"
    private Boolean activo;
    private String usuariocreacion;
    private LocalDateTime fechacreacion;
    private String usuariomodificacion;
    private LocalDateTime fechamodificacion;
}
