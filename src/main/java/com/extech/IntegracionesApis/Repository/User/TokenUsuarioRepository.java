package com.extech.IntegracionesApis.Repository.User;

import com.extech.IntegracionesApis.Domain.Model.TokenUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenUsuarioRepository extends JpaRepository<TokenUsuario, Integer> {
    Optional<TokenUsuario> findByUsuarioIdAndActivoTrue(Integer usuarioId);
    List<TokenUsuario> findByUsuarioId(Integer usuarioId);
    List<TokenUsuario> findByActivoTrueAndFechaFinVigenciaBefore(LocalDateTime fecha);
    void deleteByTokenHash(String tokenHash);
}
