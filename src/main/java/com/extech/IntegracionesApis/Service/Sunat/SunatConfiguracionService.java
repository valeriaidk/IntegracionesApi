package com.extech.IntegracionesApis.Service.Sunat;

import com.extech.IntegracionesApis.Domain.Model.ApiServices;
import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Repository.General.ApiRepository;
import com.extech.IntegracionesApis.Repository.General.ApiServicesFuncionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiExternaFuncionRepository;
import com.extech.IntegracionesApis.Repository.Sunat.SunatLogRepository;
import com.extech.IntegracionesApis.Util.Security.TokenEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SunatConfiguracionService {

    @Autowired
    private ApiRepository apiRepository;
    
    @Autowired
    private ApiServicesFuncionRepository apiServicesFuncionRepository;
    
    @Autowired
    private ApiExternaFuncionRepository apiExternaFuncionRepository;
    
    @Autowired
    private SunatLogRepository sunatLogRepository;
    
    private TokenEncryptionUtil encryptionUtil = new TokenEncryptionUtil();
    private ObjectMapper objectMapper = new ObjectMapper();

    private String apiNombre = "Decolecta";
    private String apiCodigo = "DECOLECTA";

    @Transactional
    public ApiExternaFuncion guardarConfiguracionSunat(String token) throws Exception {
        // Buscar o crear Api Decolecta
        ApiServices api = apiRepository.findByCodigo(apiCodigo)
                .orElseGet(() -> {
                    ApiServices nuevo = new ApiServices();
                    nuevo.setNombre(apiNombre);
                    nuevo.setCodigo(apiCodigo);
                    nuevo.setDescripcion("API " + apiNombre);
                    nuevo.setActivo(true);
                    nuevo.setEliminado(false);
                    nuevo.setFechaRegistro(java.time.LocalDateTime.now());
                    nuevo.setFechaModificacion(java.time.LocalDateTime.now());
                    return apiRepository.save(nuevo);
                });

        // Buscar o crear ApiServicesFuncion Sunat
        ApiServicesFuncion funcion = apiServicesFuncionRepository.findByCodigo("SUNAT_RUC")
                .orElseGet(() -> {
                    ApiServicesFuncion nueva = new ApiServicesFuncion();
                    nueva.setApiServiceId(api.getApiServiceId());
                    nueva.setNombre("Consulta RUC - SUNAT");
                    nueva.setCodigo("SUNAT_RUC");
                    nueva.setDescripcion("Función Consulta RUC");
                    nueva.setEndpoint("https://api.decolecta.com/v1/sunat/ruc?numero=");
                    nueva.setMetodo("GET");
                    nueva.setActivo(true);
                    nueva.setEliminado(false);
                    nueva.setFechaRegistro(java.time.LocalDateTime.now());
                    nueva.setFechaModificacion(java.time.LocalDateTime.now());
                    return apiServicesFuncionRepository.save(nueva);
                });

        // Crear ApiExternaFuncion
        ApiExternaFuncion externaFuncion = new ApiExternaFuncion();
        externaFuncion.setNombre("SUNAT RUC Externo");
        externaFuncion.setCodigo("SUNAT_RUC_EXT");
        externaFuncion.setDescripcion("Configuración externa para SUNAT");
        externaFuncion.setEndpoint("https://api.decolecta.com/v1/sunat/ruc?numero=");
        externaFuncion.setMetodo("GET");
        externaFuncion.setToken(encryptionUtil.encrypt(token));
        externaFuncion.setActivo(true);
        externaFuncion.setEliminado(false);
        externaFuncion.setFechaRegistro(java.time.LocalDateTime.now());
        externaFuncion.setFechaModificacion(java.time.LocalDateTime.now());

        return apiExternaFuncionRepository.save(externaFuncion);
    }

    @Transactional
    public void inicializarConfiguracionSunat() throws Exception {
        guardarConfiguracionSunat("sk_2014.E4cobzgiX8cn7zwD2xdDLHYXdzTeCOSh");
    }
}
