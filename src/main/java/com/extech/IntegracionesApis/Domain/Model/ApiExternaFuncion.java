package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_ApiExternaFuncion")
public class ApiExternaFuncion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApiExternaFuncionId")
    private Integer apiExternaFuncionId;

    // Relación con IT_ApiServicesFuncion
    @ManyToOne
    @JoinColumn(name = "ApiServicesFuncionId", nullable = false)
    private ApiServicesFuncion apiServicesFuncion;

    @Column(name = "UrlEndpoint", length = 500, nullable = false)
    private String urlEndpoint;

    @Column(name = "MetodoHttp", length = 10, nullable = false)
    private String metodoHttp;

    @Column(name = "TimeoutMs", nullable = false)
    private Integer timeoutMs;

    @Column(name = "MaxReintentos", nullable = false)
    private Integer maxReintentos;

    @Column(name = "RequiereAutenticacion", nullable = false)
    private Boolean requiereAutenticacion;

    @Column(name = "CredencialUsuario", length = 255)
    private String credencialUsuario;

    @Column(name = "CredencialClave", length = 255)
    private String credencialClave;

    @Column(name = "CabecerasExtra", columnDefinition = "text")
    private String cabecerasExtra;

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

    public ApiExternaFuncion() {
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
