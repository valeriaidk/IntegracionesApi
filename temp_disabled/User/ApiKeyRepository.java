package com.extech.IntegracionesApis.Repository.User;

import com.extech.IntegracionesApis.Domain.Model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Integer> {

    Optional<ApiKey> findByKeyValue(String keyValue);

    Optional<ApiKey> findByUsuario_UsuarioIdAndActivo(Integer usuarioId, Boolean activo);

    boolean existsByKeyValue(String keyValue);
}
