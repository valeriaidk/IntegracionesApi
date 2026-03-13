package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UsuarioId")
    private Integer usuarioId;


    @Column(name = "Nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "Apellido", length = 100, nullable = false)
    private String apellido;

    @Column(name = "Email", length = 150, nullable = false, unique = true)
    private String email;

    @Column(name = "PasswordHash", length = 256, nullable = false)
    private String passwordHash;

    @Column(name = "Telefono", length = 20)
    private String telefono;

    @Column(name = "RazonSocial", length = 200)
    private String razonSocial;

    @Column(name = "RUC", length = 20)
    private String ruc;

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

    public Usuario() {
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
