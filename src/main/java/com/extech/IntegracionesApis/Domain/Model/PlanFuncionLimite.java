package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_PlanFuncionLimite")
public class PlanFuncionLimite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LimiteId")
    private Integer limiteId;

    @Column(name = "PlanId", nullable = false)
    private Integer planId;

    @Column(name = "ApiServicesFuncionId", nullable = false)
    private Integer apiServicesFuncionId;

    @Column(name = "TipoLimite", length = 50, nullable = false)
    private String tipoLimite;

    @Column(name = "Limite", nullable = false)
    private Integer limite;

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

    public PlanFuncionLimite() {
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
