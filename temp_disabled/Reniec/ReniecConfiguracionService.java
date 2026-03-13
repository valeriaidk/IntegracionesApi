package com.extech.IntegracionesApis.Service.Reniec;

import com.extech.IntegracionesApis.Domain.Model.Api;
import com.extech.IntegracionesApis.Domain.Model.ApiFuncion;
import com.extech.IntegracionesApis.Domain.Model.ConfiguracionApiFuncion;
import com.extech.IntegracionesApis.Repository.General.ApiFuncionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiRepository;
import com.extech.IntegracionesApis.Repository.Reniec.ReniecConfiguracionRepository;
import com.extech.IntegracionesApis.Util.Security.TokenEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReniecConfiguracionService {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ApiFuncionRepository apiFuncionRepository;

    @Autowired
    private ReniecConfiguracionRepository configRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    private TokenEncryptionUtil encryptionUtil = new TokenEncryptionUtil();

    private String apiNombre = "Decolecta";
    private String apiCodigo = "DECOLECTA";

    @Transactional
    public ConfiguracionApiFuncion guardarConfiguracionReniec(String token) throws Exception {
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

        // Buscar o crear ApiFuncion Reniec
        ApiFuncion funcion = apiFuncionRepository.findByCodigo("RENIEC_DNI")
                .orElseGet(() -> {
                    ApiFuncion nueva = new ApiFuncion();
                    nueva.setApi(api);
                    nueva.setNombre("Consulta DNI - RENIEC");
                    nueva.setCodigo("RENIEC_DNI");
                    nueva.setDescripcion("Función Consulta DNI");
                    nueva.setEndpoint("https://api.decolecta.com/v1/reniec/dni?numero=");
                    nueva.setMetodo("GET");
                    nueva.setActivo(true);
                    nueva.setEliminado(false);
                    nueva.setFechaRegistro(java.time.LocalDateTime.now());
                    nueva.setFechaModificacion(java.time.LocalDateTime.now());
                    return apiFuncionRepository.save(nueva);
                });

        // Guardar configuración
        ConfiguracionApiFuncion config = configRepository
                .findByFuncion_FuncionId(funcion.getFuncionId())
                .orElse(new ConfiguracionApiFuncion());

        config.setFuncion(funcion);
        config.setUrlEndpoint("https://api.decolecta.com/v1/reniec/dni?numero=");
        config.setMetodoHttp("GET");
        config.setRequiereAutenticacion(true);
        config.setTimeoutMs(30000);  // 30 segundos
        config.setMaxReintentos(3);   // 3 reintentos
        config.setCredencialClave(encryptionUtil.encrypt(token));
        config.setActivo(true);
        config.setEliminado(false);

        return configRepository.save(config);
    }

    @Transactional
    public void inicializarConfiguracionReniec() throws Exception {
        guardarConfiguracionReniec("sk_2014.E4cobzgiX8cn7zwD2xdDLHYXdzTeCOSh");
    }
}
