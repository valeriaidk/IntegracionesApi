package com.extech.IntegracionesApis.Repository.Sms;

import com.extech.IntegracionesApis.Domain.Model.Consumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SmsRepository extends JpaRepository<Consumo, Integer> {

    Optional<Consumo> findByConsumoId(Integer consumoId);
    
    List<Consumo> findByUsuarioId(Integer usuarioId);
    
    List<Consumo> findByApiServicesFuncionId(Integer apiServicesFuncionId);
    
    List<Consumo> findByExito(Boolean exito);
    
    List<Consumo> findByUsuarioIdAndExito(Integer usuarioId, Boolean exito);
    
    @Query("SELECT c FROM Consumo c WHERE c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin")
    List<Consumo> findByFechaRegistroBetween(@Param("fechaInicio") LocalDateTime fechaInicio, 
                                           @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT c FROM Consumo c WHERE c.usuarioId = :usuarioId AND c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin")
    List<Consumo> findByUsuarioIdAndFechaRegistroBetween(@Param("usuarioId") Integer usuarioId,
                                                        @Param("fechaInicio") LocalDateTime fechaInicio,
                                                        @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT COUNT(c) FROM Consumo c WHERE c.exito = true AND c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin")
    Long countSuccessfulConsumosBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                       @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT COUNT(c) FROM Consumo c WHERE c.exito = false AND c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin")
    Long countFailedConsumosBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                   @Param("fechaFin") LocalDateTime fechaFin);
    
    @Query("SELECT c.apiServicesFuncionId, COUNT(c) FROM Consumo c WHERE c.fechaRegistro BETWEEN :fechaInicio AND :fechaFin GROUP BY c.apiServicesFuncionId")
    List<Object[]> countConsumosByApiFunctionBetween(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                   @Param("fechaFin") LocalDateTime fechaFin);
}
