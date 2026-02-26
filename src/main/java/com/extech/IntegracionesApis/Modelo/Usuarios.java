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
public class Usuarios {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idusuario;

    @ManyToOne
    @JoinColumn(name = "idempresa")
    private Empresa empresa;
    private String nombreusuario;
    private String contrasena;
    private String email;
    private Integer nivelacceso;
    private Boolean activo;
    private Integer intentosfallidos;
    private LocalDateTime ultimologin;
    private LocalDateTime fechacreacion;
}