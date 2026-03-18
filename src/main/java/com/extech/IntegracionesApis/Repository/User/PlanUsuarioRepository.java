package com.extech.IntegracionesApis.Repository.User;

import com.extech.IntegracionesApis.Domain.Model.PlanUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanUsuarioRepository extends JpaRepository<PlanUsuario, Integer> {
    Optional<PlanUsuario> findByUsuarioIdAndActivoTrueAndEliminadoFalse(Integer usuarioId);
}
