# Insurance Policy Service

**Backend REST para gestión de clientes y pólizas de seguros.**

## 📋 Tabla de Contenidos

1. [Solución Planteada](#solución-planteada)
2. [Modelo de Datos](#modelo-de-datos)
3. [Arquitectura](#arquitectura)
4. [Instalación y Ejecución Local](#instalación-y-ejecución-local)
5. [API REST - Documentación](#api-rest-documentación)
6. [Pruebas](#pruebas)
7. [Arquitectura AWS Propuesta](#arquitectura-aws-propuesta)
8. [Stack Tecnológico](#stack-tecnológico)

---

## 🎯 Solución Planteada

### Contexto
Esta es una solución de **backend para una aseguradora grande (+40M clientes)** que necesita gestionar:
- Clientes (personas aseguradas)
- Pólizas de seguros con múltiples tipos (Vida, Vehículo, Salud)
- Relaciones complejas (beneficiarios, vehículos, dependientes)
- Validaciones de reglas de negocio

### Decisiones Técnicas

| Decisión | Justificación |
|----------|--------------|
| **Spring Boot 3.2** | Framework maduro, escalable, con excelente soporte para REST y JPA |
| **H2 Database (en memoria)** | Facilita desarrollo/testing sin requerir BD externa. Portable para demostración. |
| **Spring Data JPA** | ORM estándar, reduce boilerplate, permite migraciones automáticas |
| **Lombok** | Reduce código repetitivo (getters, setters, constructores, toString) |
| **SpringDoc OpenAPI** | Generación automática de Swagger/OpenAPI 3.0 con documentación integrada |
| **Validaciones en Service** | Lógica de negocio centralizada, fácil de testear y mantener |
| **@ControllerAdvice** | Manejo centralizado de excepciones, respuestas HTTP normalizadas |
| **JUnit 5 + Mockito** | Testing estándar en Java, cobertura de 80%+ esperada |

### Reglas de Negocio Implementadas

✅ **Póliza de Vida**
- Solo 1 póliza de Vida por cliente
- Máximo 2 beneficiarios por póliza
- Validación al crear (lanza excepción 400 si ya existe)

✅ **Póliza de Vehículo**
- N vehículos por póliza (o múltiples pólizas)
- Placa única en el sistema

✅ **Póliza de Salud**
- Modelado de dependientes por relación (Cliente, Padre, Madre, Cónyuge, Hijo)
- Tarifa por dependiente
- Listado de beneficiarios/dependientes por póliza

✅ **Clientes**
- Documento único (tipoDocumento + numeroDocumento)
- Email único
- Datos personales completos

---

## 📊 Modelo de Datos

### Diagrama de Entidades (Conceptual)

```
┌─────────────┐
│   Client    │
├─────────────┤
│ id (PK)     │
│ tipoDoc     │ ← Único (con numeroDocumento)
│ numeroDoc   │ ← Único (con tipoDocumento)
│ nombres     │
│ apellidos   │
│ email       │ ← Único
│ teléfono    │
│ fechaNac    │
│ createdAt   │
│ updatedAt   │
└──────┬──────┘
       │ 1:N
       │
   ┌───┴─────────┐
   │   Policy    │
   ├─────────────┤
   │ id (PK)     │
   │ policyNo    │ ← Único
   │ type        │ ← ENUM (VIDA/VEHICULO/SALUD)
   │ clientId(FK)│
   │ startDate   │
   │ endDate     │
   │ premium     │
   │ status      │
   │ createdAt   │
   │ updatedAt   │
   └──┬──────────┘
      │
      ├─→ 1:N Beneficiary (VIDA)
      ├─→ 1:N Vehicle (VEHICULO)
      └─→ 1:N Dependent (SALUD)
```

### Tablas Principales

| Tabla | Descripción | Relaciones |
|-------|-------------|-----------|
| `clients` | Personas aseguradas | PK: id, UK: tipoDocumento+numeroDocumento, UK: email |
| `policies` | Pólizas de seguros | FK: client_id, ENUM: policy_type |
| `beneficiaries` | Beneficiarios de pólizas de Vida | FK: policy_id (max 2 per policy) |
| `vehicles` | Vehículos asegurados | FK: policy_id, UK: plate |
| `dependents` | Dependientes de pólizas de Salud | FK: policy_id |

### Validaciones en Base de Datos

- **Unique Constraints**: tipoDocumento+numeroDocumento, email (clientes); policyNumber (pólizas); plate (vehículos)
- **Foreign Keys**: Cascada elimina pólizas/beneficiarios/vehículos cuando se elimina cliente
- **Check Constraints** (en BD): startDate < endDate, premium > 0 (en aplicación)

---

## 🏗️ Arquitectura

### Estructura de Capas (Clean Architecture)

```
com.insurance
├── domain/                      ← LOGICA DE NEGOCIO PURA
│   ├── model/                   (Entidades JPA, ValueObjects, Enums)
│   │   ├── Client.java
│   │   ├── Policy.java
│   │   ├── PolicyType.java
│   │   ├── Beneficiary.java
│   │   ├── Vehicle.java
│   │   └── Dependent.java
│   ├── service/                 (Servicios de dominio - opcional si hay lógica)
│   └── repository/              (Interfaces de acceso a datos)
│       ├── ClientRepository.java
│       ├── PolicyRepository.java
│       ├── BeneficiaryRepository.java
│       ├── VehicleRepository.java
│       └── DependentRepository.java
│
├── application/                 ← CASOS DE USO / ORQUESTACION
│   ├── usecase/
│   │   ├── ClientService.java   (CRUD clientes, validaciones)
│   │   └── PolicyService.java   (CRUD pólizas, reglas negocio)
│   └── dto/                     (DTOs para REST: Requests/Responses)
│       ├── ClientCreateRequest.java
│       ├── ClientResponse.java
│       ├── PolicyCreateRequest.java
│       ├── PolicyResponse.java
│       ├── BeneficiaryCreateRequest.java
│       ├── BeneficiaryResponse.java
│       ├── VehicleCreateRequest.java
│       ├── VehicleResponse.java
│       ├── DependentCreateRequest.java
│       ├── DependentResponse.java
│       └── ErrorResponse.java
│
├── infrastructure/              ← DETALLES TECNICOS
│   ├── rest/                    (Controllers REST)
│   │   ├── ClientController.java
│   │   ├── PolicyController.java
│   │   └── HealthController.java
│   ├── persistence/             (JPA, mappers - aquí vivirían custom mappers si aplica)
│   └── config/                  (Configuraciones Spring)
│       └── GlobalExceptionHandler.java
│
└── shared/                      ← UTILERIAS COMPARTIDAS
    ├── exception/               (Excepciones personalizadas)
    │   ├── InsuranceException.java
    │   ├── ResourceNotFoundException.java
    │   ├── BusinessRuleException.java
    │   └── ValidationException.java
    └── utils/                   (Utilidades generales)
```

### Flujo de una Solicitud HTTP

```
CLIENT
  │
  ├─→ HTTP POST /api/v1/clients
  │
  ├─→ ClientController.createClient()
  │
  ├─→ ClientService.createClient()
  │   ├─ Validar datos
  │   ├─ Verificar documento único
  │   ├─ Verificar email único
  │   └─ Persistir con ClientRepository
  │
  ├─ Si error → GlobalExceptionHandler
  │   └─ ErrorResponse (400/404/500)
  │
  └─ Si éxito → ClientResponse (201)
```

---

## 🚀 Instalación y Ejecución Local

### Requisitos Previos

- **Java 17+** ([descargar](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html))
- **Maven 3.8+** ([descargar](https://maven.apache.org/download.cgi))
- **Git** (opcional, para clonar)

### Pasos de Instalación

#### 1. Clonar o descargar el proyecto
```bash
cd c:\Users\diego\OneDrive\Documentos\Documentos\ Personales\Prueba\ Tecnica\insurance-policy-service
```

#### 2. Compilar el proyecto
```bash
mvn clean install
```

Esto descargará las dependencias y compilará el código. Si es la primera vez, puede tardar algunos minutos.

#### 3. Ejecutar la aplicación

**Opción A: Desde Maven**
```bash
mvn spring-boot:run
```

**Opción B: Desde JAR compilado**
```bash
java -jar target/insurance-policy-service-1.0.0.jar
```

#### 4. Verificar que está corriendo

La aplicación se levanta en `http://localhost:8080` (configurable en `application.properties`).

**Health Check:**
```bash
curl http://localhost:8080/api/v1/health
```

Respuesta esperada:
```json
{
  "status": "UP",
  "service": "Insurance Policy Service",
  "version": "1.0.0"
}
```

---

## � Autenticación y Seguridad

### Overview
La aplicación utiliza **Spring Security 3.2.0** con autenticación basada en **JWT (JSON Web Tokens)** para proteger los endpoints. El flujo de autenticación es:

1. Usuario se registra o hace login
2. Sistema valida credenciales contra base de datos
3. Genera un JWT firmado (HS512) con expiración configurable
4. Cliente incluye el JWT en todas las peticiones posteriores
5. Sistema valida el token en cada request

### Configuración (Variables de Entorno)

Edita el archivo `.env` en la raíz del proyecto (copiado de `.env.example`):

```bash
# JWT Configuration
JWT_SECRET=your-super-secret-key-min-32-characters-long-for-HS512
JWT_EXPIRATION=86400000    # Access token: 24 horas en millisegundos
JWT_REFRESH_EXPIRATION=604800000  # Refresh token: 7 días

# CORS Configuration
CORS_ORIGINS=http://localhost:3000,http://localhost:4200
CORS_METHODS=GET,POST,PUT,DELETE,OPTIONS
CORS_HEADERS=*
CORS_MAX_AGE=3600

# Security Headers
SECURITY_HEADERS_ENABLED=true
```

### Roles de Usuario

| Rol | Autoridad | Descripción |
|-----|-----------|-------------|
| ADMIN | ROLE_ADMIN | Administrador del sistema |
| AGENT | ROLE_AGENT | Agente de seguros |
| CUSTOMER | ROLE_CUSTOMER | Cliente del seguro |

### Endpoints de Autenticación

#### 1. **POST /api/v1/auth/register** - Registrar nuevo usuario

Crea un nuevo usuario en el sistema.

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "MiContraseña123!",
    "confirmPassword": "MiContraseña123!",
    "fullName": "Juan Pérez García"
  }'
```

**Respuesta (201 Created):**
```json
{
  "message": "Usuario registrado exitosamente",
  "data": {
    "access_token": "eyJhbGciOiJIUzUxMiJ9...",
    "refresh_token": "eyJhbGciOiJIUzUxMiJ9...",
    "token_type": "Bearer",
    "expires_in": 86400,
    "username": "usuario@example.com",
    "email": "usuario@example.com"
  }
}
```

**Validaciones:**
- Email único y válido (formato RFC 5322)
- Contraseña mínimo 8 caracteres
- Las dos contraseñas deben coincidir
- Nombre completo entre 2 y 100 caracteres

---

#### 2. **POST /api/v1/auth/login** - Login con credenciales

Autentica un usuario existente.

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "password": "MiContraseña123!"
  }'
```

**Respuesta (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNjc3NjAwMDAwLCJleHAiOjE2Nzc2ODYwMDB9...",
  "refresh_token": "eyJhbGciOiJIUzUxMiJ9...",
  "token_type": "Bearer",
  "expires_in": 86400,
  "username": "usuario@example.com",
  "email": "usuario@example.com"
}
```

**Respuestas de error:**
- `401 Unauthorized` - Email o contraseña inválidos
- `400 Bad Request` - Datos incompletos o inválidos

---

#### 3. **POST /api/v1/auth/refresh** - Refrescar token

Obtiene un nuevo access_token usando el refresh_token (sin necesidad de login).

```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }'
```

**Respuesta (200 OK):**
```json
{
  "access_token": "eyJhbGciOiJIUzUxMiJ9.NEW_TOKEN...",
  "refresh_token": "eyJhbGciOiJIUzUxMiJ9...",
  "token_type": "Bearer",
  "expires_in": 86400,
  "username": "usuario@example.com"
}
```

---

#### 4. **GET /api/v1/auth/validate** - Validar token

Verifica si un JWT token es válido y no ha expirado.

```bash
curl -X GET "http://localhost:8080/api/v1/auth/validate?token=eyJhbGciOiJIUzUxMiJ9..."
```

**Respuesta (200 OK):**
```json
true
```

---

#### 5. **POST /api/v1/auth/logout** - Logout

Invalida la sesión del usuario (basado en cliente borrar el token).

```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
```

**Respuesta (200 OK):** No tiene contenido

---

### Cómo usar el JWT en las peticiones

Después de obtener el token, inclúyelo en el header `Authorization` de todas las peticiones:

```bash
curl -X GET http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c3VhcmlvQGV4YW1wbGUuY29tIiwiaWF0IjoxNjc3NjAwMDAwLCJleHAiOjE2Nzc2ODYwMDB9..."
```

**Si el token es inválido o expirado:**
- `401 Unauthorized` - Token inválido, expirado o ausente

---

### Endpoints Protegidos vs Públicos

| Endpoint | Protección | Token Requerido |
|----------|-----------|-----------------|
| POST /auth/register | Público | ❌ No |
| POST /auth/login | Público | ❌ No |
| POST /auth/refresh | Público | ❌ No |
| GET /auth/validate | Público | ❌ No |
| POST /auth/logout | Público | ❌ No (Informativo) |
| GET /api/v1/clients | Público | ❌ No (Solo lectura) |
| GET /api/v1/policies | Público | ❌ No (Solo lectura) |
| POST /api/v1/clients | 🔒 Protegido | ✅ Sí |
| PUT /api/v1/clients/{id} | 🔒 Protegido | ✅ Sí |
| DELETE /api/v1/clients/{id} | 🔒 Protegido | ✅ Sí |
| POST /api/v1/policies | 🔒 Protegido | ✅ Sí |
| PUT /api/v1/policies/{id} | 🔒 Protegido | ✅ Sí |

---

## �📚 API REST - Documentación

### Swagger/OpenAPI

Una vez la app está corriendo, accede a:

🔗 **http://localhost:8080/swagger-ui.html**

Aquí verás:
- Todos los endpoints agrupados por tags (Clientes, Pólizas, Health)
- Esquemas de request/response
- Ejemplos de payloads
- Botón "Try it out" para probar endpoints

### Endpoints Principales

#### **CLIENTES**

| Método | Endpoint | Descripción | Respuesta |
|--------|----------|-------------|-----------|
| POST | `/api/v1/clients` | Crear cliente | 201 Created |
| GET | `/api/v1/clients/{id}` | Obtener cliente | 200 OK |
| GET | `/api/v1/clients` | Listar clientes | 200 OK |
| GET | `/api/v1/clients/search?tipoDocumento=CC&numeroDocumento=1234567890` | Buscar por documento | 200 OK |
| PUT | `/api/v1/clients/{id}` | Actualizar cliente | 200 OK |
| DELETE | `/api/v1/clients/{id}` | Eliminar cliente | 204 No Content |

**Ejemplo: Crear Cliente**

```bash
curl -X POST http://localhost:8080/api/v1/clients \
  -H "Content-Type: application/json" \
  -d '{
    "tipoDocumento": "CC",
    "numeroDocumento": "1234567890",
    "nombres": "Juan",
    "apellidos": "Pérez García",
    "email": "juan.perez@example.com",
    "telefono": "+57 3001234567",
    "fechaNacimiento": "1990-05-15"
  }'
```

**Respuesta (201):**
```json
{
  "id": 1,
  "tipoDocumento": "CC",
  "numeroDocumento": "1234567890",
  "nombres": "Juan",
  "apellidos": "Pérez García",
  "email": "juan.perez@example.com",
  "telefono": "+57 3001234567",
  "fechaNacimiento": "1990-05-15",
  "fullName": "Juan Pérez García",
  "createdAt": "2026-02-28",
  "updatedAt": "2026-02-28"
}
```

#### **PÓLIZAS**

| Método | Endpoint | Descripción | Respuesta |
|--------|----------|-------------|-----------|
| POST | `/api/v1/policies` | Crear póliza | 201 Created |
| GET | `/api/v1/policies/{id}` | Obtener póliza | 200 OK |
| GET | `/api/v1/policies/client/{clientId}` | Listar pólizas de cliente | 200 OK |
| GET | `/api/v1/policies/client/{clientId}/type/{type}` | Listar por tipo | 200 OK |
| POST | `/api/v1/policies/{policyId}/beneficiaries` | Agregar beneficiario (VIDA) | 201 Created |
| GET | `/api/v1/policies/{policyId}/beneficiaries` | Listar beneficiarios (VIDA) | 200 OK |
| POST | `/api/v1/policies/{policyId}/vehicles` | Agregar vehículo (VEHICULO) | 201 Created |
| POST | `/api/v1/policies/{policyId}/dependents` | Agregar dependiente (SALUD) | 201 Created |
| GET | `/api/v1/policies/{policyId}/dependents` | Listar dependientes (SALUD) | 200 OK |
| PUT | `/api/v1/policies/{id}/status?status=CANCELADA` | Cambiar estado | 200 OK |
| DELETE | `/api/v1/policies/{id}` | Eliminar póliza | 204 No Content |

**Ejemplo: Crear Póliza de Vida**

```bash
curl -X POST http://localhost:8080/api/v1/policies \
  -H "Content-Type: application/json" \
  -d '{
    "clientId": 1,
    "policyType": "VIDA",
    "startDate": "2026-03-01",
    "endDate": "2027-03-01",
    "premiumAmount": 500000.00,
    "status": "ACTIVA"
  }'
```

**Respuesta (201):**
```json
{
  "id": 1,
  "policyNumber": "POL-2026-123456",
  "policyType": "VIDA",
  "clientId": 1,
  "clientName": "Juan Pérez García",
  "startDate": "2026-03-01",
  "endDate": "2027-03-01",
  "premiumAmount": 500000.00,
  "status": "ACTIVA",
  "isActive": true,
  "beneficiaries": [],
  "vehicles": [],
  "dependents": [],
  "createdAt": "2026-02-28",
  "updatedAt": "2026-02-28"
}
```

**Ejemplo: Agregar Beneficiario a Póliza de Vida**

```bash
curl -X POST http://localhost:8080/api/v1/policies/1/beneficiaries \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "María García Pérez",
    "documentNumber": "9876543210",
    "relationship": "Cónyuge",
    "benefitPercentage": 50.0
  }'
```

### Códigos de Error

| Status | Código Error | Significado |
|--------|--------------|------------|
| 400 | VALIDATION_ERROR | Datos inválidos o campos faltantes |
| 400 | BUSINESS_RULE_VIOLATION | Violación de regla de negocio (ej: póliza VIDA duplicada) |
| 404 | RESOURCE_NOT_FOUND | Recurso (cliente/póliza) no encontrado |
| 500 | INTERNAL_SERVER_ERROR | Error inesperado del servidor |

**Ejemplo: Error 400**
```json
{
  "status": 400,
  "errorCode": "VIDA_POLICY_ALREADY_EXISTS",
  "message": "El cliente ya posee una póliza de Vida. Solo se permite 1 póliza de Vida por cliente.",
  "path": "/api/v1/policies",
  "timestamp": "2026-02-28T10:30:15"
}
```

---

## 🧪 Pruebas

### Ejecutar Tests

```bash
# Ejecutar todos los tests
mvn test

# Ejecutar tests con cobertura (JaCoCo)
mvn test jacoco:report

# Ver reporte de cobertura
# Abierto: target/site/jacoco/index.html
```

### Cobertura Esperada

El proyecto está estructurado para alcanzar **80%+ de cobertura**:
- **Service layer**: 85%+ (lógica de negocio compleja)
- **Controller layer**: 80%+ (validaciones HTTP, mapeo DTOs)
- **Repository layer**: <50% (delegado a Spring Data)

### Ejemplos de Tests (estructura)

```java
// Tests para ClientService
@SpringBootTest
class ClientServiceTest {
    @MockBean private ClientRepository repository;
    @InjectMocks private ClientService service;
    
    @Test
    void createClient_Success() { /* test */ }
    
    @Test
    void createClient_DuplicateDocument_ThrowsException() { /* test */ }
    
    @Test
    void getClientById_NotFound_ThrowsException() { /* test */ }
}

// Tests para PolicyService (reglas de negocio)
@SpringBootTest
class PolicyServiceTest {
    @MockBean private PolicyRepository policyRepository;
    @InjectMocks private PolicyService service;
    
    @Test
    void createVidaPolicy_AlreadyExists_ThrowsBusinessRuleException() { /* test */ }
    
    @Test
    void addBeneficiary_MaxExceeded_ThrowsException() { /* test */ }
}

// Tests para ClientController (MockMvc)
@WebMvcTest(ClientController.class)
class ClientControllerTest {
    @MockBean private ClientService service;
    @Autowired private MockMvc mockMvc;
    
    @Test
    void createClient_Returns201() throws Exception {
        mockMvc.perform(post("/api/v1/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("..."))
            .andExpect(status().isCreated());
    }
}
```

---

## ☁️ Arquitectura AWS Propuesta

### Escenario: Migración a Producción en AWS

```
┌────────────────────────────────────────────────────────────────┐
│                         AWS (Producción)                       │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│  Route 53 (DNS)                                               │
│      │                                                         │
│      ├─→ ALB (Application Load Balancer)                      │
│             │                                                 │
│             ├─→ ECS Fargate (Contenedores Spring Boot)       │
│             │   - Auto Scaling (2-10 instances)             │
│             │   - Health Checks                              │
│             │                                                 │
│             └─→ RDS Aurora PostgreSQL                        │
│                 - Multi-AZ (Alta disponibilidad)            │
│                 - Backups automáticos (30 días)             │
│                 - Read Replicas                              │
│                                                                │
│  CloudWatch (Monitoreo)                                       │
│      └─→ Logs, Métricas, Alertas                            │
│                                                                │
│  VPC con Subnets privadas (para BD)                          │
│  VPC con Subnets públicas (para ALB)                         │
│  Security Groups (restricción de acceso)                     │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

### Pasos para Migración

#### 1. **Containerizar la Aplicación**

Crear `Dockerfile`:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/insurance-policy-service-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Construir imagen Docker:
```bash
docker build -t insurance-policy-service:1.0.0 .
docker push [ECR_REGISTRY]/insurance-policy-service:1.0.0
```

#### 2. **Provisionar Base de Datos RDS Aurora PostgreSQL**

```yaml
RDS Aurora PostgreSQL:
  - Instances: 2 (Primary + Standby Multi-AZ)
  - Engine: aurora-postgresql 15
  - Storage: 100 GB inicial
  - Backups: Automáticos (30 días)
  - Performance Insights: Habilitado
```

**Actualizar application.properties para RDS:**
```properties
spring.datasource.url=jdbc:postgresql://aurora.c1234567890.us-east-1.rds.amazonaws.com:5432/insurance
spring.datasource.username=${DB_USERNAME}  # De Secrets Manager
spring.datasource.password=${DB_PASSWORD}  # De Secrets Manager
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect
spring.jpa.hibernate.ddl-auto=validate    # En producción, NO usar create-drop
```

#### 3. **Desplegar en ECS Fargate**

```bash
# Push imagen a ECR (Elastic Container Registry)
aws ecr get-login-password | docker login --username AWS --password-stdin [ECR_REGISTRY]
docker push [ECR_REGISTRY]/insurance-policy-service:1.0.0

# Crear Task Definition en ECS
# - CPU: 512, Memoria: 1024 MB (ajustar según carga)
# - Port: 8080
# - Environment: Pasar variables desde Secrets Manager

# Crear ECS Service con Auto Scaling
# - Desired Count: 2
# - Min: 2, Max: 10
# - Scaling Policy: Target CPU 70%
```

#### 4. **Configurar ALB (Application Load Balancer)**

```yaml
ALB Configuration:
  - Target Group:
      Protocol: HTTP
      Port: 8080
      Health Check: GET /api/v1/health
      Interval: 30s
      Timeout: 5s
      Healthy Threshold: 2
      Unhealthy Threshold: 3
  
  - Listener:
      Protocol: HTTPS (con ACM Certificate)
      Port: 443
      Forward to Target Group
      
  - Redirect HTTP → HTTPS
```

#### 5. **Monitoreo y Logs con CloudWatch**

```yaml
CloudWatch:
  - Logs: /aws/ecs/insurance-policy-service
    - Log Level: INFO (debug en desarrollo)
    - Retention: 30 días
  
  - Métricas:
      - CPUUtilization (target: 70%)
      - MemoryUtilization (target: 80%)
      - ECS TaskCount (min 2, max 10)
      - RDS Connections
      - ALB Request Count
      - HTTP 4xx/5xx Errors
  
  - Alarms:
      - Error Rate > 5%
      - Response Time > 2s
      - Database Connection Pool Exhausted
```

#### 6. **Continuidad Empresarial**

```yaml
Disaster Recovery:
  - RDS Multi-Region Backup: Global Database
  - Replicación a región secundaria (us-west-2)
  - RPO (Recovery Point Objective): 1 hora
  - RTO (Recovery Time Objective): 5 minutos
  
Seguridad:
  - VPC Endpoints para servicios AWS
  - Security Groups: Ingress solo desde ALB
  - IAM Roles: Mínimos permisos para acceder RDS/Secrets
  - Secrets Manager: Credenciales BD rotadas cada 30 días
  - KMS Encryption: En tránsito (TLS) y en reposo
```

#### 7. **Estimación de Costos (Mensual)**

| Servicio | Características | Costo Estimado |
|----------|-----------------|----------------|
| **ECS Fargate** | 2-10 tasks (512 CPU, 1 GB) | $100-300 |
| **RDS Aurora** | 2 instances db.t3.micro, 100 GB | $150-200 |
| **ALB** | 1 ALB con listeners/rules | $50-100 |
| **CloudWatch** | Logs, Métricas, Alarms | $50-100 |
| **Route 53** | Zona DNS, queries | $10-20 |
| **NAT Gateway** | Outbound traffic | $30-50 |
| **Total Estimado** | | **$390-770/mes** |

---

## 💻 Stack Tecnológico

### Backend
- **Java 17**: Lenguaje de programación
- **Spring Boot 3.2.0**: Framework principal
- **Spring Data JPA**: ORM y acceso a datos
- **Spring Web**: REST APIs
- **H2 Database**: Base de datos en memoria (desarrollo)
- **PostgreSQL**: Recomendado para producción
- **Lombok**: Reducción de boilerplate
- **SpringDoc OpenAPI 2.1.0**: Swagger/OpenAPI 3.0

### Testing & Quality
- **JUnit 5**: Framework de testing
- **Mockito**: Mocking
- **Spring Test/MockMvc**: Testing de controladores
- **JaCoCo**: Análisis de cobertura

### Build & Tools
- **Maven 3.8+**: Gestor de dependencias y build
- **Git**: Control de versiones

### Otros
- **SLF4J + Logback**: Logging
- **Jackson**: Serialización JSON

---

## 📝 Notas Importantes

### Auditoría y Logs
- Cada entidad tiene `createdAt` y `updatedAt` con hooks `@PrePersist` y `@PreUpdate`
- Logs en nivel INFO para producción, DEBUG para desarrollo
- Global exception handler registra errores en CloudWatch (AWS) o logs locales

### Seguridad
- **OWASP Top 10**: Validación de inputs, SQL injection prevention (JPA parameterizado)
- **CORS**: Configurar según necesidad en producción
- **Authentication/Authorization**: Agregar Spring Security + JWT para producción
- **HTTPS**: Obligatorio en producción (ALB con ACM)

### Escalabilidad
- **Stateless**: Cada instancia es independiente (horizontal scaling)
- **Connection Pooling**: HikariCP (por defecto en Spring Boot)
- **Caching**: Opcional con Redis si se requiere (Spring Cache)
- **CQRS/Event Sourcing**: Futuro si complejidad aumenta

### Mejoras Futuras
- [ ] Agregar Spring Security + JWT para autenticación
- [ ] Implementar Spring Cache para pólizas frecuentes
- [ ] Agregar Spring Batch para reportes/migraciones
- [ ] Implementar API Gateway para versionado (v1, v2, etc.)
- [ ] Agregar circuit breaker (Hystrix/Resilience4j)
- [ ] Considerar Event-Driven Architecture para cambios de póliza

---

## 🤝 Contribución y Soporte

Cualquier pregunta o sugerencia sobre la arquitectura o implementación, contactar al equipo de desarrollo.

**Última actualización:** Febrero 28, 2026
