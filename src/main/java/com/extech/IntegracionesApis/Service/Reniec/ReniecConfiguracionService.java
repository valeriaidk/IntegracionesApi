package com.extech.IntegracionesApis.Service.Reniec;

import com.extech.IntegracionesApis.Domain.Model.ApiServices;
import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Repository.General.ApiRepository;
import com.extech.IntegracionesApis.Repository.General.ApiServicesFuncionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiExternaFuncionRepository;
import com.extech.IntegracionesApis.Repository.Reniec.ReniecConfiguracionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReniecConfiguracionService {

    @Autowired
    private ApiRepository apiRepository;
    
    @Autowired
    private ApiServicesFuncionRepository apiServicesFuncionRepository;
    
    @Autowired
    private ApiExternaFuncionRepository apiExternaFuncionRepository;
    
    @Autowired
    private ReniecConfiguracionRepository reniecConfiguracionRepository;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    private String apiNombre = "Decolecta";
    private String apiCodigo = "DECOLECTA";

    @Transactional
    public ApiExternaFuncion guardarConfiguracionReniec(String token) throws Exception {
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

        // Buscar o crear ApiServicesFuncion Reniec
        ApiServicesFuncion funcion = apiServicesFuncionRepository.findByCodigo("RENIEC_DNI")
                .orElseGet(() -> {
                    ApiServicesFuncion nueva = new ApiServicesFuncion();
                    nueva.setApiServiceId(api.getApiServiceId());
                    nueva.setNombre("Consulta DNI - RENIEC");
                    nueva.setCodigo("RENIEC_DNI");
                    nueva.setDescripcion("Función Consulta DNI");
                    nueva.setEndpoint("https://api.decolecta.com/v1/reniec/dni?numero=");
                    nueva.setMetodo("GET");
                    nueva.setActivo(true);
                    nueva.setEliminado(false);
                    nueva.setFechaRegistro(java.time.LocalDateTime.now());
                    nueva.setFechaModificacion(java.time.LocalDateTime.now());
                    return apiServicesFuncionRepository.save(nueva);
                });

        // Crear ApiExternaFuncion
        ApiExternaFuncion externaFuncion = new ApiExternaFuncion();
        externaFuncion.setNombre("RENIEC DNI Externo");
        externaFuncion.setCodigo("RENIEC_DNI_EXT");
        externaFuncion.setDescripcion("Configuración externa para RENIEC");
        externaFuncion.setEndpoint("https://api.decolecta.com/v1/reniec/dni?numero=");
        externaFuncion.setMetodo("GET");
        externaFuncion.setToken(token);
        externaFuncion.setActivo(true);
        externaFuncion.setEliminado(false);
        externaFuncion.setFechaRegistro(java.time.LocalDateTime.now());
        externaFuncion.setFechaModificacion(java.time.LocalDateTime.now());

        return apiExternaFuncionRepository.save(externaFuncion);
    }

    @Transactional
    public void inicializarConfiguracionReniec() throws Exception {
        guardarConfiguracionReniec("sk_2014.E4cobzgiX8cn7zwD2xdDLHYXdzTeCOSh");
    }
}
