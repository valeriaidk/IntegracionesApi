package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_ApiServicesFuncion")
public class ApiServicesFuncion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApiServicesFuncionId")
    private Integer apiServicesFuncionId;

    // RELACIÓN CON IT_Api
    @ManyToOne
    @JoinColumn(name = "ApiId", nullable = false)
    private ApiServices api;

    @Column(name = "Nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "Codigo", length = 50, nullable = false)
    private String codigo;

    @Column(name = "Descripcion", length = 500)
    private String descripcion;

    @Column(name = "Endpoint", length = 300, nullable = false)
    private String endpoint;

    @Column(name = "Request", columnDefinition = "nvarchar(MAX)", nullable = false)
    private String request;

    @Column(name = "Response", columnDefinition = "nvarchar(MAX)", nullable = false)
    private String response;

    @Column(name = "Metodo", length = 10, nullable = false)
    private String metodo;

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

    public ApiServicesFuncion() {
    }

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
        this.eliminado = false;
    }

    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }
}