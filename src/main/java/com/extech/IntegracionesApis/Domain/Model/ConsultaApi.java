package com.extech.IntegracionesApis.Domain.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "IT_Consultas")

public class ConsultaApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConsultaId")
    private Integer consultaId;

    @Column(name = "BaseUrl", length = 500, nullable = false)
    private String baseUrl;

    @Column(name = "HttpMethod", length = 10, nullable = false)
    private String httpMethod;

    @Column(name = "AuthHeaderName", length = 100)
    private String authHeaderName;
q
    @Column(name = "Token", length = 1000)
    private String token;

    @Column(name = "Documento", length = 20, nullable = false)
    private String documento;

    @Column(name = "Activo", nullable = false)
    private Boolean activo;

    @Column(name = "UsuarioRegistro")
    private Integer usuarioRegistro;

    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "UsuarioModificacion")
    private Integer usuarioModificacion;

    @Column(name = "FechaModificacion")
    private LocalDateTime fechaModificacion;

}
