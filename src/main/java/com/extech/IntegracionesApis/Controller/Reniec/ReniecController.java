package com.extech.IntegracionesApis.Controller.Reniec;

import com.extech.IntegracionesApis.Service.Reniec.ReniecService;
import com.extech.IntegracionesApis.Util.Security.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para consultas a RENIEC
 * 
 * Este controlador expone los endpoints para consultar datos de DNI y RUC
 * utilizando el nuevo flujo de autenticación y resolución de configuración.
 * 
 * Flujo implementado:
 * 1. Validación de token de usuario en ApiKeyAuthFilter
 * 2. Obtención de UsuarioId desde UserContext
 * 3. Resolución de configuración externa via SP uspObtenerConfiguracionApiExternaPorUsuario
 * 4. Consumo del proveedor externo real
 * 5. Registro de auditoría en IT_Consumo
 * 
 * @author Extech
 * @version 2.0
 * @since 2026-03-16
 */
@RestController
@RequestMapping("/api/reniec")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RENIEC", description = """
    ## API para consulta de datos con RENIEC mediante configuración dinámica
    
    ### Características principales:
    - Autenticación mediante bearer token de usuario
    - Resolución automática de configuración del proveedor
    - Auditoría completa de consumos en IT_Consumo
    - Manejo seguro de tokens de proveedores externos
    - Soporte para múltiples proveedores mediante base de datos
    
    ### Flujo de autenticación:
    1. Cliente envía bearer token de usuario
    2. ApiKeyAuthFilter valida token y extrae UsuarioId
    3. UsuarioId se almacena en UserContext para uso en servicios
    4. Servicios usan UsuarioId para resolver configuración externa
    
    ### Códigos de función soportados:
    - RENIEC_DNI: Consulta de DNI
    - RENIEC_RUC: Consulta de RUC
    
    ### Configuración requerida en base de datos:
    - IT_ApiServicesFuncion: Definición de funciones internas
    - IT_ApiAsignacion: Relación entre funciones internas y externas
    - IT_ApiExternaFuncion: Configuración de proveedores externos
    """)
public class ReniecController {

    private final ReniecService reniecService;

    /**
     * Consulta documento en RENIEC usando el nuevo flujo de configuración dinámica
     * 
     * @param tipo Tipo de documento (DNI o RUC)
     * @param numero Número de documento a consultar
     * @return ResponseEntity con los datos consultados o error
     */
    @GetMapping("/consultar/{tipo}/{numero}")
    @Operation(
            summary = "Consultar documento en RENIEC",
            description = """
            ### Permite consultar información de DNI o RUC a través del servicio de RENIEC
            
            **Flujo interno:**
            1. Validación de token de usuario (automático por filtro)
            2. Obtención de UsuarioId desde contexto de seguridad
            3. Resolución de configuración externa mediante SP
            4. Consumo del proveedor RENIEC configurado
            5. Registro de auditoría del consumo
            
            **Tipos soportados:**
            - DNI: Consulta de datos de persona natural
            - RUC: Consulta de datos de persona jurídica
            
            **Requisitos:**
            - Bearer token válido en header Authorization
            - Usuario con configuración RENIEC asignada
            - Permiso para consumir la función específica
            """,
            parameters = {
                @Parameter(name = "tipo", description = "Tipo de documento: DNI o RUC", required = true, example = "DNI"),
                @Parameter(name = "numero", description = "Número de documento a consultar", required = true, example = "71234567")
            },
            responses = {
                @ApiResponse(responseCode = "200", description = "Consulta exitosa"),
                @ApiResponse(responseCode = "400", description = "Tipo de documento inválido"),
                @ApiResponse(responseCode = "401", description = "No autenticado o token inválido"),
                @ApiResponse(responseCode = "403", description = "Configuración RENIEC no encontrada para el usuario"),
                @ApiResponse(responseCode = "500", description = "Error interno del servidor")
            }
    )
    public ResponseEntity<?> consultarDocumento(
            @PathVariable String tipo,
            @PathVariable String numero) {
        
        // Verificar que hay un usuario autenticado en el contexto
        Integer usuarioId = UserContext.getUsuarioId();
        if (usuarioId == null) {
            log.error("No hay usuario autenticado en el contexto para consulta RENIEC");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(crearErrorResponse("AUTH_ERROR", "Usuario no autenticado"));
        }

        log.info("Usuario {} consultando {} {} en RENIEC", usuarioId, tipo, numero);

        try {
            // Validar tipo de documento
            if (!"DNI".equalsIgnoreCase(tipo) && !"RUC".equalsIgnoreCase(tipo)) {
                return ResponseEntity.badRequest()
                        .body(crearErrorResponse("INVALID_TYPE", "Tipo de documento inválido. Use DNI o RUC"));
            }

            // Validar formato del número según el tipo
            String validationResult = validarFormatoDocumento(tipo, numero);
            if (validationResult != null) {
                return ResponseEntity.badRequest()
                        .body(crearErrorResponse("INVALID_FORMAT", validationResult));
            }

            // Realizar consulta según el tipo
            String resultado;
            if ("DNI".equalsIgnoreCase(tipo)) {
                resultado = reniecService.consultarDNI(numero);
            } else { // RUC
                resultado = reniecService.consultarRUC(numero);
            }

            log.info("Consulta RENIEC exitosa para usuario: {} - {} {}", usuarioId, tipo, numero);
            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            log.error("Error consultando {} {} en RENIEC para usuario: {}", tipo, numero, usuarioId, e);
            
            // Clasificar el error según el mensaje
            String errorCode = "RENIEC_ERROR";
            String errorMessage = e.getMessage();
            
            if (errorMessage.contains("no autenticado")) {
                errorCode = "AUTH_ERROR";
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(crearErrorResponse(errorCode, errorMessage));
            } else if (errorMessage.contains("no configurada") || errorMessage.contains("no encontrada")) {
                errorCode = "CONFIG_ERROR";
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(crearErrorResponse(errorCode, errorMessage));
            } else if (errorMessage.contains("incompleta")) {
                errorCode = "CONFIG_INCOMPLETE";
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(crearErrorResponse(errorCode, errorMessage));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(crearErrorResponse(errorCode, errorMessage));
        }
    }

    /**
     * Valida el formato del documento según el tipo
     * 
     * @param tipo Tipo de documento (DNI/RUC)
     * @param numero Número a validar
     * @return Mensaje de error si hay problema, null si es válido
     */
    private String validarFormatoDocumento(String tipo, String numero) {
        if (numero == null || numero.trim().isEmpty()) {
            return "Número de documento es requerido";
        }

        numero = numero.trim();

        if ("DNI".equalsIgnoreCase(tipo)) {
            if (!numero.matches("\\d{8}")) {
                return "DNI debe tener exactamente 8 dígitos";
            }
        } else if ("RUC".equalsIgnoreCase(tipo)) {
            if (!numero.matches("\\d{11}")) {
                return "RUC debe tener exactamente 11 dígitos";
            }
        }

        return null;
    }

    /**
     * Crea una respuesta de error estandarizada
     * 
     * @param errorCode Código del error
     * @param errorMessage Mensaje descriptivo del error
     * @return Map con la estructura de error
     */
    private Map<String, Object> crearErrorResponse(String errorCode, String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("errorMessage", errorMessage);
        errorResponse.put("timestamp", java.time.LocalDateTime.now());
        errorResponse.put("service", "RENIEC");
        return errorResponse;
    }
}