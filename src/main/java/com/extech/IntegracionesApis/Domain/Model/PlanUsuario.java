package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_PlanUsuario")
public class PlanUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PlanUsuarioId")
    private Integer planUsuarioId;

    @Column(name = "UsuarioId", nullable = false)
    private Integer usuarioId;

    @Column(name = "PlanId", nullable = false)
    private Integer planId;

    @Column(name = "FechaInicioVigencia", nullable = false)
    private LocalDateTime fechaInicioVigencia;

    @Column(name = "FechaFinVigencia")
    private LocalDateTime fechaFinVigencia;

    @Column(name = "EstadoSuscripcion", length = 50)
    private String estadoSuscripcion;

    @Column(name = "Observacion", length = 500)
    private String observacion;

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

    public PlanUsuario() {
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
