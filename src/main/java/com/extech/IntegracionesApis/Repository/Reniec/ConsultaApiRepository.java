package com.extech.IntegracionesApis.Repository.Reniec;

import com.extech.IntegracionesApis.Domain.Model.ConsultaApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultaApiRepository extends JpaRepository<ConsultaApi, Integer> {
    ConsultaApi findByDocumentoAndActivo(String documento, Boolean activo);
}