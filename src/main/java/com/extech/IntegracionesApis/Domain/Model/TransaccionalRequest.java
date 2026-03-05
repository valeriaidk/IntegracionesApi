package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "it_TransaccionalRequest")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TransaccionId")
    private Long transaccionId;

    @Column(name = "UsuarioId", nullable = false)
    private Integer usuarioId;

    @Column(name = "RequestBody", columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "ResponseBody", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "Estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "CodigoHttp")
    private Integer codigoHttp;

    @Column(name = "TiempoRespuestaMs")
    private Integer tiempoRespuestaMs;

    @Column(name = "ProveedorRef", length = 100)
    private String proveedorRef;

    @Column(name = "Intentos", nullable = false)
    private Integer intentos;

    @Column(name = "MensajeError", length = 500)
    private String mensajeError;

    @Column(name = "IpOrigen", length = 50)
    private String ipOrigen;

    @Column(name = "UsuarioRegistro", length = 100)
    private String usuarioRegistro;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "UsuarioModificacion", length = 100)
    private String usuarioModificacion;

    @Column(name = "FechaModificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;

    @Column(name = "Eliminado", nullable = false)
    private Boolean eliminado;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (fechaRegistro == null) {
            fechaRegistro = now;
        }
        if (activo == null) {
            activo = true;
        }
        if (eliminado == null) {
            eliminado = false;
        }
        if (intentos == null) {
            intentos = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
