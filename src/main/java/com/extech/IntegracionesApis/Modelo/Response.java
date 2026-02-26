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

public class Response {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idresponse;

    @ManyToOne
    @JoinColumn(name = "idrequest")
    private Request request;

    @Column(columnDefinition = "TEXT")
    private String respuesta;
    private String mensajeerror;
    private String proveedorref;
    private Boolean exitoso;
    private LocalDateTime fechacreacion;
}
