package com.extech.IntegracionesApis.Modelo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idtemplate;

    @ManyToOne
    @JoinColumn(name = "idpermiso")
    private Permisos permiso;
    private String canal;
    private String nombre;
    private String asunto;

    @Column(columnDefinition = "TEXT")
    private String contenido;
    private Integer version;
    private Boolean activo;
    private String usuariocreacion;
    private LocalDateTime fechacreacion;
    private String usuariomodificacion;
    private LocalDateTime fechamodificacion;
}
