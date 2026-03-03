package com.extech.IntegracionesApis.Api.ModeloApi;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_Api")

public class Api {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApiId")
    private Integer apiId;

    @Column(name = "Nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "Codigo", length = 50, nullable = false)
    private String codigo;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @Column(name = "UsuarioRegistro")
    private Integer usuarioRegistro;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "UsuarioModificacion")
    private Integer usuarioModificacion;

    @Column(name = "FechaModificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;

    @Column(name = "Eliminado", nullable = false)
    private Boolean eliminado;

}
