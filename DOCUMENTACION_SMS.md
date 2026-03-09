# API SMS - Documentación Completa

## 📋 Tabla de Contenidos

1. [Descripción General](#descripción-general)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Configuración](#configuración)
4. [Endpoints](#endpoints)
5. [Modelos de Datos](#modelos-de-datos)
6. [Flujo de Envío](#flujo-de-envío)
7. [Manejo de Errores](#manejo-de-errores)
8. [Base de Datos](#base-de-datos)
9. [Ejemplos de Uso](#ejemplos-de-uso)
10. [Troubleshooting](#troubleshooting)

---

## 📖 Descripción General

La API SMS permite el envío de mensajes de texto a través del proveedor Infobip, con persistencia en base de datos para seguimiento y estadísticas.

### Características Principales
- ✅ Envío de SMS individual y masivo
- ✅ Validación de números y mensajes
- ✅ Persistencia completa en base de datos
- ✅ Estadísticas y reportes
- ✅ Manejo de errores específico
- ✅ Logging detallado para diagnóstico

---

## 🏗️ Arquitectura del Sistema

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Swagger UI    │    │   Controller    │    │     Service      │
│   (Documenta)   │───▶│   (SmsController)│───▶│   (SmsService)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Postman/CLI   │    │   Repository    │    │   Infobip API   │
│   (Pruebas)     │───▶│   (SmsRepository)│───▶│   (Externo)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   Base de Datos │
                       │   (SQL Server)  │
                       └─────────────────┘
```

---

## ⚙️ Configuración

### application.properties
```properties
# Configuración de Infobip SMS API
infobit.api.url=https://api.infobip.com/sms/2/text
infobit.api.key=TU_API_KEY_REAL_DE_INFOBIP
infobit.api.sender=INFOBIT

# Configuración de timeouts
spring.http.client.connect-timeout=10000
spring.http.client.read-timeout=30000
```

### Dependencias Principales
- Spring Boot 4.0.3
- Spring Data JPA
- SpringDoc OpenAPI (Swagger)
- SQL Server Driver
- Lombok

---

## 🔌 Endpoints

### 1. Verificar Configuración
```http
GET /api/v1/sms/test-config
GET /api/v1/sms/validate-config
```

### 2. Envío de SMS
```http
POST /api/v1/sms/send
POST /api/v1/sms/send-batch
```

### 3. Consulta y Seguimiento
```http
GET /api/v1/sms/status/{messageId}
GET /api/v1/sms/history/{phoneNumber}
GET /api/v1/sms/history?fechaInicio=...&fechaFin=...
```

### 4. Estadísticas
```http
GET /api/v1/sms/statistics
```

---

## 📊 Modelos de Datos

### SmsRequest (DTO de Entrada)
```json
{
  "phoneNumber": "+51987654321",
  "message": "Tu mensaje aquí",
  "senderId": "INFOBIT",
  "campaignName": "Campana2024"
}
```

### SmsResponse (DTO de Salida)
```json
{
  "success": true,
  "messageId": "MSG_1715278800000",
  "phoneNumber": "+51987654321",
  "statusCode": "200",
  "statusMessage": "SMS enviado exitosamente",
  "timestamp": "2026-03-09T09:23:00",
  "provider": "Infobit"
}
```

### Sms (Entidad de Base de Datos)
```java
@Entity
@Table(name = "IT_Sms")
public class Sms {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long smsId;
    
    @Column(name = "PhoneNumber", nullable = false)
    private String phoneNumber;
    
    @Column(name = "Message", columnDefinition = "TEXT", nullable = false)
    private String message;
    
    @Column(name = "MessageId")
    private String messageId;
    
    @Column(name = "Success", nullable = false)
    private Boolean success;
    
    @Column(name = "StatusCode")
    private String statusCode;
    
    @Column(name = "StatusMessage")
    private String statusMessage;
    
    @Column(name = "ErrorCode")
    private String errorCode;
    
    @Column(name = "ErrorMessage")
    private String errorMessage;
    
    @Column(name = "Provider", nullable = false)
    private String provider;
    
    @Column(name = "FechaEnvio", nullable = false)
    private LocalDateTime fechaEnvio;
    
    @Column(name = "FechaRegistro", nullable = false)
    private LocalDateTime fechaRegistro;
    
    // ... otros campos
}
```

---

## 🔄 Flujo de Envío

```
1. Cliente → POST /api/v1/sms/send
   ↓
2. Controller → SmsService.sendSms()
   ↓
3. Service → Validar request
   ↓
4. Service → Crear entidad Sms
   ↓
5. Service → Llamar API Infobip
   ↓
6. Service → Procesar respuesta
   ↓
7. Service → Guardar en BD
   ↓
8. Service → Retornar SmsResponse
   ↓
9. Controller → Cliente final
```

---

## ⚠️ Manejo de Errores

### Tipos de Errores Manejados

#### 1. Error de Autenticación (401)
```json
{
  "success": false,
  "errorCode": "AUTH_ERROR",
  "errorMessage": "API Key inválida. Verifica tu configuración en application.properties",
  "phoneNumber": "+51987654321"
}
```

#### 2. Error de Cliente HTTP (4xx)
```json
{
  "success": false,
  "errorCode": "HTTP_CLIENT_ERROR",
  "errorMessage": "Error HTTP: 400 - Bad Request",
  "phoneNumber": "+51987654321"
}
```

#### 3. Error de Servidor (5xx)
```json
{
  "success": false,
  "errorCode": "HTTP_SERVER_ERROR",
  "errorMessage": "Error del servidor Infobip",
  "phoneNumber": "+51987654321"
}
```

#### 4. Error de Conexión
```json
{
  "success": false,
  "errorCode": "CONNECTION_ERROR",
  "errorMessage": "No se puede conectar al servicio Infobip",
  "phoneNumber": "+51987654321"
}
```

#### 5. Error Interno
```json
{
  "success": false,
  "errorCode": "INTERNAL_ERROR",
  "errorMessage": "Error interno: NullPointerException",
  "phoneNumber": "+51987654321"
}
```

---

## 🗄️ Base de Datos

### Tabla: IT_Sms
| Columna | Tipo | Descripción |
|---------|------|-------------|
| SmsId | BIGINT IDENTITY | Primary Key |
| PhoneNumber | VARCHAR(20) NOT NULL | Número de teléfono |
| Message | TEXT NOT NULL | Mensaje enviado |
| MessageId | VARCHAR(100) | ID del mensaje de Infobip |
| Success | BIT NOT NULL | Si el envío fue exitoso |
| StatusCode | VARCHAR(20) | Código de estado |
| StatusMessage | VARCHAR(500) | Mensaje de estado |
| ErrorCode | VARCHAR(50) | Código de error |
| ErrorMessage | VARCHAR(500) | Mensaje de error |
| Provider | VARCHAR(50) NOT NULL | Proveedor del servicio |
| FechaEnvio | DATETIME2 NOT NULL | Fecha de envío |
| FechaRegistro | DATETIME2 NOT NULL | Fecha de registro |

### Índices Recomendados
```sql
CREATE INDEX IX_SMS_PhoneNumber ON IT_Sms(PhoneNumber);
CREATE INDEX IX_SMS_FechaEnvio ON IT_Sms(FechaEnvio);
CREATE INDEX IX_SMS_MessageId ON IT_Sms(MessageId);
CREATE INDEX IX_SMS_Success ON IT_Sms(Success);
```

---

## 💡 Ejemplos de Uso

### 1. Enviar SMS Individual
```bash
curl -X POST http://localhost:8081/api/v1/sms/send \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+51987654321",
    "message": "Hola, este es un mensaje de prueba",
    "senderId": "INFOBIT"
  }'
```

### 2. Enviar SMS Masivo
```bash
curl -X POST http://localhost:8081/api/v1/sms/send-batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "phoneNumber": "+51987654321",
      "message": "Mensaje 1"
    },
    {
      "phoneNumber": "+51987654322",
      "message": "Mensaje 2"
    }
  ]'
```

### 3. Consultar Historial
```bash
# Por número
curl http://localhost:8081/api/v1/sms/history/+51987654321

# Por rango de fechas
curl "http://localhost:8081/api/v1/sms/history?fechaInicio=2026-03-01T00:00:00&fechaFin=2026-03-09T23:59:59"
```

### 4. Obtener Estadísticas
```bash
curl "http://localhost:8081/api/v1/sms/statistics?fechaInicio=2026-03-01T00:00:00&fechaFin=2026-03-09T23:59:59"
```

---

## 🔧 Troubleshooting

### Problemas Comunes

#### 1. Error 401 Unauthorized
**Causa:** API Key inválida o no configurada
**Solución:**
```properties
# Reemplaza con tu API Key real de Infobip
infobit.api.key=c677accbbac0a1a23a2cbcf485e5b1e8-b531c34c-d646-4239-b67c-7a7194643255
```

#### 2. Error de Conexión
**Causa:** Problemas de red o URL incorrecta
**Solución:**
```properties
# Verifica la URL
infobit.api.url=https://api.infobip.com/sms/2/text
```

#### 3. Error de Validación
**Causa:** Formato de número inválido
**Solución:**
```json
{
  "phoneNumber": "+51987654321",  // Formato correcto: +51 + 9 dígitos
  "message": "Mensaje válido"
}
```

#### 4. Error de Base de Datos
**Causa:** Conexión a BD fallida
**Solución:**
```properties
# Verifica credenciales de BD
spring.datasource.url=jdbc:sqlserver://101.44.10.88;databaseName=BDExtech_Utilitarios;encrypt=true;trustServerCertificate=true
spring.datasource.username=usrExtechQas
spring.datasource.password=Qa5*2o/25-Ext
```

### Logs Importantes

#### Envío Exitoso
```
INFO  --- [IntegracionesApis] : Enviando SMS al número: +51987654321
INFO  --- [IntegracionesApis] : API URL: https://api.infobip.com/sms/2/text
INFO  --- [IntegracionesApis] : API Key (primeros 10 chars): c677accbbac...
INFO  --- [IntegracionesApis] : SMS enviado exitosamente. Response: {"messageId":"12345","status":"sent"}
```

#### Error de Autenticación
```
ERROR --- [IntegracionesApis] : ERROR DE AUTENTICACIÓN: API Key inválida o no autorizada. Status: 401, Body: {"error":"Unauthorized"}
```

---

## 📈 Métricas y Monitoreo

### KPIs Disponibles
- ✅ Tasa de entrega exitosa
- ✅ Tiempo promedio de respuesta
- ✅ Errores por tipo
- ✅ SMS por proveedor
- ✅ Volumen diario/mensual

### Endpoints de Monitoreo
- `/api/v1/sms/statistics` - Estadísticas completas
- `/actuator/health` - Salud del servicio
- `/actuator/metrics` - Métricas de Spring Boot

---

## 🔐 Seguridad

### Consideraciones
- ✅ API Keys almacenadas en application.properties
- ✅ Validación de entrada de datos
- ✅ Logging sin información sensible
- ✅ Manejo seguro de errores

### Recomendaciones
- 🔒 Usar variables de entorno para API Keys
- 🔒 Implementar rate limiting
- 🔒 Agregar autenticación a los endpoints
- 🔒 Encriptar datos sensibles en BD

---

## 🚀 Mejores Prácticas

### Desarrollo
- ✅ Usar logs estructurados
- ✅ Manejar todos los casos de error
- ✅ Validar entrada de datos
- ✅ Documentar endpoints

### Producción
- ✅ Configurar timeouts adecuados
- ✅ Implementar retry policies
- ✅ Monitorear métricas
- ✅ Tener plan de contingencia

---

## 📞 Soporte

### Contacto
- **Desarrollador:** Equipo Extech
- **Documentación:** Swagger UI en `/swagger-ui/index.html`
- **Logs:** Ver consola de aplicación

### Recursos
- [Documentación Infobip](https://www.infobip.com/docs/api)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [SpringDoc OpenAPI](https://springdoc.org/)

---

*Última actualización: 9 de Marzo, 2026*
