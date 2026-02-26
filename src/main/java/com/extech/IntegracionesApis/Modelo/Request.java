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

public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idrequest;

    @ManyToOne
    @JoinColumn(name = "idpermiso")
    private Permisos permiso;
    private String estado;
    private String tokenunico;
    private Integer reintentos;

    @Column(columnDefinition = "TEXT")
    private String datossolicitados;
    private String procesadopor;
    private String usuariocreacion;
    private LocalDateTime fechacreacion;
    private String usuariomodificacion;
    private LocalDateTime fechamodificacion;
    private LocalDateTime fechaprocesado;
}
