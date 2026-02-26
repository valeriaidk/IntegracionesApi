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

public class Empresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idempresa;
    private String codigo;
    private String nombre;
    private Boolean activo;
    private LocalDateTime fechacreacion;
}
