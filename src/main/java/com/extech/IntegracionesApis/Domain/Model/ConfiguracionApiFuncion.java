package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_ConfiguracionApiFuncion")
public class ConfiguracionApiFuncion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConfigApiFuncionId")
    private Integer configApiFuncionId;

    // Relación con IT_ApiFuncion
    @ManyToOne
    @JoinColumn(name = "FuncionId", nullable = false)
    private ApiFuncion funcion;

    @Column(name = "UrlEndpoint", length = 500, nullable = false)
    private String urlEndpoint;

    @Column(name = "MetodoHttp", length = 10)
    private String metodoHttp;

    @Column(name = "TimeoutMs")
    private Integer timeoutMs;

    @Column(name = "MaxReintentos")
    private Integer maxReintentos;

    // @Column(name = "IntervaloReintegroSeg")
    // private Integer intervaloReintegroSeg;

    @Column(name = "RequiereAutenticacion")
    private Boolean requiereAutenticacion;

    @Column(name = "CredencialUsuario", length = 255)
    private String credencialUsuario;

    @Column(name = "CredencialClave", length = 255)
    private String credencialClave;

    @Column(name = "CabecerasExtra", columnDefinition = "text")
    private String cabecerasExtra;

    @Column(name = "UsuarioRegistro", length = 100)
    private String usuarioRegistro;

    @Column(name = "FechaRegistro")
    private LocalDateTime fechaRegistro;

    @Column(name = "UsuarioModificacion", length = 100)
    private String usuarioModificacion;

    @Column(name = "FechaModificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "Activo")
    private Boolean activo;

    @Column(name = "Eliminado")
    private Boolean eliminado;

    // @Column(name = "ConfiguracionId")
    // private Integer configuracionId;

    // @Column(name = "id_configuracion")
    // private Integer idConfiguracion;

    public ConfiguracionApiFuncion() {
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
