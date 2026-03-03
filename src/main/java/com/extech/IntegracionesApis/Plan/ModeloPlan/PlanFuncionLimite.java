package com.extech.IntegracionesApis.Plan.ModeloPlan;

import com.extech.IntegracionesApis.Api.ModeloApi.ApiFuncion;
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

    // 🔥 Relación con Plan
    @ManyToOne
    @JoinColumn(name = "PlanId", nullable = false)
    private Plan plan;

    // 🔥 Relación con ApiFuncion
    @ManyToOne
    @JoinColumn(name = "FuncionId", nullable = false)
    private ApiFuncion funcion;

    @Column(name = "LimiteMensual", nullable = false)
    private Integer limiteMensual;

    @Column(name = "LimiteDiario", nullable = false)
    private Integer limiteDiario;

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