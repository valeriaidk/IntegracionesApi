package com.extech.IntegracionesApis.Repository.General;

import com.extech.IntegracionesApis.Domain.Model.ApiService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiRepository extends JpaRepository<ApiService, Integer> {
    Optional<ApiService> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}
