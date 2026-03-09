package com.extech.IntegracionesApis.Service.Sunat;

import com.extech.IntegracionesApis.Domain.Model.Api;
import com.extech.IntegracionesApis.Domain.Model.ApiFuncion;
import com.extech.IntegracionesApis.Domain.Model.ConfiguracionApiFuncion;
import com.extech.IntegracionesApis.Repository.General.ApiFuncionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiRepository;
import com.extech.IntegracionesApis.Repository.Sunat.SunatConfiguracionRepository;
import com.extech.IntegracionesApis.Util.Security.TokenEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SunatConfiguracionService {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiFuncionRepository apiFuncionRepository;

    @Autowired
    private SunatConfiguracionRepository configuracionRepository;

    private TokenEncryptionUtil encryptionUtil = new TokenEncryptionUtil();

    private String apiNombre = "Decolecta";
    private String apiCodigo = "DECOLECTA";

    @Transactional
    public ConfiguracionApiFuncion guardarConfiguracionSunat(String token) throws Exception {
        // Buscar o crear Api Decolecta
        Api api = apiRepository.findByCodigo(apiCodigo)
                .orElseGet(() -> {
                    Api nuevo = new Api();
                    nuevo.setNombre(apiNombre);
                    nuevo.setCodigo(apiCodigo);
                    nuevo.setDescripcion("API " + apiNombre);
                    nuevo.setActivo(true);
                    nuevo.setEliminado(false);
                    nuevo.setFechaRegistro(java.time.LocalDateTime.now());
                    nuevo.setFechaModificacion(java.time.LocalDateTime.now());
                    return apiRepository.save(nuevo);
                });

        // Buscar o crear ApiFuncion Sunat
        ApiFuncion funcion = apiFuncionRepository.findByCodigo("SUNAT_RUC")
                .orElseGet(() -> {
                    ApiFuncion nueva = new ApiFuncion();
                    nueva.setApi(api);
                    nueva.setNombre("Consulta RUC - SUNAT");
                    nueva.setCodigo("SUNAT_RUC");
                    nueva.setDescripcion("Función Consulta RUC");
                    nueva.setEndpoint("https://api.decolecta.com/v1/sunat/ruc/full?numero=");
                    nueva.setMetodo("GET");
                    nueva.setActivo(true);
                    nueva.setEliminado(false);
                    return apiFuncionRepository.save(nueva);
                });

        // Guardar configuración
        ConfiguracionApiFuncion config = configuracionRepository
                .findByFuncion_FuncionId(funcion.getFuncionId())
                .orElse(new ConfiguracionApiFuncion());

        config.setFuncion(funcion);
        config.setUrlEndpoint("https://api.decolecta.com/v1/sunat/ruc/full?numero=");
        config.setMetodoHttp("GET");
        config.setRequiereAutenticacion(true);
        config.setTimeoutMs(30000);  // 30 segundos
        config.setMaxReintentos(3);   // 3 reintentos
        config.setCredencialClave(encryptionUtil.encrypt(token));
        config.setActivo(true);
        config.setEliminado(false);

        return configuracionRepository.save(config);
    }

    @Transactional
    public void inicializarConfiguracionSunat() throws Exception {
        guardarConfiguracionSunat("sk_2014.E4cobzgiX8cn7zwD2xdDLHYXdzTeCOSh");
    }
}
