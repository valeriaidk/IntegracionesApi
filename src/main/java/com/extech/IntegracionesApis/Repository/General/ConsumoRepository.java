package com.extech.IntegracionesApis.Repository.General;

import com.extech.IntegracionesApis.Domain.Model.Consumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsumoRepository extends JpaRepository<Consumo, Integer> {
    
    List<Consumo> findByUsuarioId(Integer usuarioId);
    
    List<Consumo> findByApiServicesFuncionId(Integer apiServicesFuncionId);
    
    List<Consumo> findByUsuarioIdAndFechaRegistroBetween(
        Integer usuarioId, 
        LocalDateTime fechaInicio, 
        LocalDateTime fechaFin
    );
    
    @Query("SELECT COUNT(c) FROM Consumo c WHERE c.usuarioId = :usuarioId AND c.apiServicesFuncionId = :apiServicesFuncionId AND c.fechaRegistro >= :fechaInicio")
    Long countConsumosByUsuarioAndFuncionDesdeFecha(
        @Param("usuarioId") Integer usuarioId,
        @Param("apiServicesFuncionId") Integer apiServicesFuncionId,
        @Param("fechaInicio") LocalDateTime fechaInicio
    );
}
