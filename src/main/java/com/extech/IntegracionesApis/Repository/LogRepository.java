package com.extech.IntegracionesApis.Repository;

import com.extech.IntegracionesApis.Domain.Model.Consumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Consumo, Integer> {
    
    List<Consumo> findByUsuarioId(Integer usuarioId);
    
    List<Consumo> findByFechaRegistroBetween(LocalDateTime inicio, LocalDateTime fin);
    
    List<Consumo> findByUsuarioIdAndFechaRegistroBetween(Integer usuarioId, LocalDateTime inicio, LocalDateTime fin);
}
