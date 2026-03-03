package com.extech.IntegracionesApis.Api.ModeloApi;

import com.extech.IntegracionesApis.Usuario.ModeloUsuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_ApiKey")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApiKeyId")
    private Integer apiKeyId;

    // 🔥 Relación con Usuario
    @ManyToOne
    @JoinColumn(name = "UsuarioId", nullable = false)
    private Usuario usuario;

    // ⚠ Aquí se guarda el HASH, no la ApiKey en texto plano
    @Column(name = "KeyValue", length = 100, nullable = false)
    private String keyValue;

    @Column(name = "FechaExpiracion")
    private LocalDateTime fechaExpiracion;

    @Column(name = "UltimoUso")
    private LocalDateTime ultimoUso;

    @Column(name = "FechaRegeneracion")
    private LocalDateTime fechaRegeneracion;

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

    public ApiKey() {
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
