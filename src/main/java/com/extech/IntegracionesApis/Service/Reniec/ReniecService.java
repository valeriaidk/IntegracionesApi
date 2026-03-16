package com.extech.IntegracionesApis.Service.Reniec;

import com.extech.IntegracionesApis.Domain.Model.Consumo;
import com.extech.IntegracionesApis.Repository.LogRepository;
import com.extech.IntegracionesApis.Repository.Sunat.SunatLogRepository;
import com.extech.IntegracionesApis.Util.Security.TokenEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ReniecService {

    @Autowired
    private LogRepository logRepository;
    
    @Autowired
    private SunatLogRepository sunatLogRepository;
    
    private TokenEncryptionUtil encryptionUtil = new TokenEncryptionUtil();
    private ObjectMapper objectMapper = new ObjectMapper();

    public String consultarDNI(String numeroDni) throws Exception {
        // Lógica para consultar DNI a RENIEC
        // Aquí implementarías la llamada real a la API
        
        // Guardar log del consumo
        Consumo consumo = new Consumo();
        consumo.setUsuarioId(1); // ID del usuario actual
        consumo.setApiServicesFuncionId(1); // ID de la función RENIEC
        consumo.setRequest("{\"dni\":\"" + numeroDni + "\"}");
        consumo.setResponse("{\"resultado\":\"datos del dni\"}");
        consumo.setExito(true);
        consumo.setActivo(true);
        consumo.setEliminado(false);
        
        logRepository.save(consumo);
        
        return "{\"resultado\":\"datos del dni " + numeroDni + "\"}";
    }
    
    public String consultarRUC(String numeroRuc) throws Exception {
        // Lógica para consultar RUC (similar a DNI)
        Consumo consumo = new Consumo();
        consumo.setUsuarioId(1);
        consumo.setApiServicesFuncionId(1);
        consumo.setRequest("{\"ruc\":\"" + numeroRuc + "\"}");
        consumo.setResponse("{\"resultado\":\"datos del ruc\"}");
        consumo.setExito(true);
        consumo.setActivo(true);
        consumo.setEliminado(false);
        
        logRepository.save(consumo);
        
        return "{\"resultado\":\"datos del ruc " + numeroRuc + "\"}";
    }

}
