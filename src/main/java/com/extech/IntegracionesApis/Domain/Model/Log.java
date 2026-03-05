package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "IT_Log")
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogId")
    private Integer logId;

    @Column(name = "TipoDocumento", length = 10, nullable = false)
    private String tipoDocumento;

    @Column(name = "NumeroDocumento", length = 20, nullable = false)
    private String numeroDocumento;

    @Column(name = "UsuarioRegistro")
    private Integer usuarioRegistro;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "HttpStatus")
    private Integer httpStatus;

    @Column(name = "Mensaje", length = 500)
    private String mensaje;

    @Column(name = "Respuesta", columnDefinition = "NVARCHAR(MAX)")
    private String respuesta;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;

    @Column(name = "UsuarioModificacion")
    private Integer usuarioModificacion;

    @Column(name = "FechaModificacion")
    private LocalDateTime fechaModificacion;
}
