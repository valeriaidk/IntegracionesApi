package com.extech.IntegracionesApis.Service.Sunat;

import com.extech.IntegracionesApis.Domain.Model.Consumo;
import com.extech.IntegracionesApis.Domain.Dto.Sunat.SunatResponse;
import com.extech.IntegracionesApis.Repository.Sunat.SunatLogRepository;
import com.extech.IntegracionesApis.Util.Security.TokenEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class SunatService {

    @Autowired
    private SunatLogRepository sunatLogRepository;
    
    private TokenEncryptionUtil encryptionUtil = new TokenEncryptionUtil();
    private ObjectMapper objectMapper = new ObjectMapper();

    public SunatResponse consultarRuc(String numeroRuc) throws Exception {
        // Lógica para consultar RUC a SUNAT
        // Aquí implementarías la llamada real a la API
        
        // Guardar log del consumo
        Consumo consumo = new Consumo();
        consumo.setUsuarioId(1); // ID del usuario actual
        consumo.setApiServicesFuncionId(2); // ID de la función SUNAT
        consumo.setRequest("{\"ruc\":\"" + numeroRuc + "\"}");
        consumo.setResponse("{\"resultado\":\"datos del ruc\"}");
        consumo.setExito(true);
        consumo.setActivo(true);
        consumo.setEliminado(false);
        
        sunatLogRepository.save(consumo);
        
        // Crear respuesta SunatResponse
        SunatResponse response = new SunatResponse();
        response.setNumero_documento(numeroRuc);
        response.setRazon_social("EMPRESA DE EJEMPLO S.A.C.");
        response.setEstado("ACTIVO");
        response.setCondicion("HABIDO");
        response.setDireccion("AV. EJEMPLO 123");
        response.setDistrito("LIMA");
        response.setProvincia("LIMA");
        response.setDepartamento("LIMA");
        response.setTipoConsulta("RUC");
        response.setLimiteConsultas(100);
        response.setMensaje("Consulta exitosa");
        response.setPlan("free");
        
        return response;
    }
    
    public SunatResponse consultarRUC(String numeroRuc) throws Exception {
        return consultarRuc(numeroRuc);
    }
}
