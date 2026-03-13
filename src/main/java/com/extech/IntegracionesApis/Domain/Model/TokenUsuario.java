package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_Token_Usuario")
public class TokenUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TokenUsuarioId")
    private Integer tokenUsuarioId;

    @Column(name = "UsuarioId", nullable = false)
    private Integer usuarioId;

    @Column(name = "TokenHash", length = 256, nullable = false)
    private String tokenHash;

    @Column(name = "FechaInicioVigencia")
    private LocalDateTime fechaInicioVigencia;

    @Column(name = "FechaFinVigencia")
    private LocalDateTime fechaFinVigencia;

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

    public TokenUsuario() {
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
