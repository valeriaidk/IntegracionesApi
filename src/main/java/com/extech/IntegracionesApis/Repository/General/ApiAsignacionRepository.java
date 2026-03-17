package com.extech.IntegracionesApis.Repository.General;

import com.extech.IntegracionesApis.Domain.Model.ApiAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiAsignacionRepository extends JpaRepository<ApiAsignacion, Integer> {
    
    @Query("SELECT a FROM ApiAsignacion a WHERE a.activo = true AND a.eliminado = false")
    List<ApiAsignacion> findActivos();
    
    @Query("SELECT a FROM ApiAsignacion a WHERE a.apiServicesFuncionId = :apiServicesFuncionId AND a.activo = true AND a.eliminado = false")
    List<ApiAsignacion> findByApiServicesFuncionId(@Param("apiServicesFuncionId") Integer apiServicesFuncionId);
    
    @Query("SELECT a FROM ApiAsignacion a WHERE a.apiExternaFuncionId = :apiExternaFuncionId AND a.activo = true AND a.eliminado = false")
    List<ApiAsignacion> findByApiExternaFuncionId(@Param("apiExternaFuncionId") Integer apiExternaFuncionId);
    
    @Query("SELECT a FROM ApiAsignacion a WHERE a.apiServicesFuncionId = :apiServicesFuncionId AND a.apiExternaFuncionId = :apiExternaFuncionId AND a.activo = true AND a.eliminado = false")
    Optional<ApiAsignacion> findByApiServicesFuncionIdAndApiExternaFuncionId(
        @Param("apiServicesFuncionId") Integer apiServicesFuncionId, 
        @Param("apiExternaFuncionId") Integer apiExternaFuncionId
    );
    
    boolean existsByApiServicesFuncionIdAndApiExternaFuncionIdAndActivoTrueAndEliminadoFalse(
        Integer apiServicesFuncionId, 
        Integer apiExternaFuncionId
    );
}
