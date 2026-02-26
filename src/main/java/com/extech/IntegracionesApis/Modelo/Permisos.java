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

public class Permisos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idpermiso;

    @ManyToOne
    @JoinColumn(name = "idusuario")
    private Usuarios usuario;
    private String codigo;
    private String canal;
    private String descripcion;
    private Integer nivelminimo;

    @Column(columnDefinition = "TEXT")
    private String configjson;
    private Boolean activo;
    private String usuariocreacion;
    private LocalDateTime fechacreacion;
    private String usuariomodificacion;
    private LocalDateTime fechamodificacion;
}
