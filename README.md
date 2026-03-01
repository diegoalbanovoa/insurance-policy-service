# Insurance Policy Service

Servicio backend (API REST) para la gestión de **clientes** y **pólizas de seguros** (Vida, Vehículo y Salud), desarrollado como solución para una prueba técnica. La implementación incluye persistencia local (H2), validación de reglas de negocio, manejo centralizado de errores, documentación con OpenAPI/Swagger y pruebas unitarias.

---

## Tabla de contenidos

- [Alcance de la solución](#alcance-de-la-solución)
- [Reglas de negocio](#reglas-de-negocio)
- [Modelo de datos](#modelo-de-datos)
- [Arquitectura](#arquitectura)
- [API y documentación](#api-y-documentación)
- [Ejecución local](#ejecución-local)
- [Pruebas](#pruebas)
- [Propuesta de despliegue en AWS](#propuesta-de-despliegue-en-aws)

---

## Alcance de la solución

La API cubre las operaciones mínimas solicitadas para:

- **Clientes**
  - Crear, consultar, actualizar y eliminar.
- **Pólizas**
  - Crear pólizas asociadas a un cliente.
  - Listar pólizas por cliente.
  - Consultar el detalle de una póliza.
  - Listar beneficiarios/dependientes asociados a pólizas de salud.

La persistencia se realiza en una base de datos **H2** para facilitar la ejecución local sin dependencias externas.

---

## Reglas de negocio

La capa de aplicación valida las reglas principales descritas en el enunciado:

### Póliza de Vida
- Un cliente puede tener **máximo 1 póliza de vida**.
- Una póliza de vida admite **máximo 2 beneficiarios**.

### Póliza de Vehículo
- Un cliente puede asegurar **N vehículos**, ya sea en una póliza o en múltiples pólizas (dependiendo de la forma de creación).
- La placa del vehículo se maneja como un dato relevante para evitar duplicidades según el diseño implementado.

### Póliza de Salud
La póliza de salud permite modelar coberturas típicas:
- Solo cliente.
- Cliente + padres.
- Cliente + cónyuge + hijos.

> Nota: el objetivo de esta entrega es el modelado/gestión y la validación de estructura del grupo cubierto. Si existiera cálculo de tarifas, se podría extender como una regla adicional de dominio.

---

## Modelo de datos

### Entidades principales
- **Client**
- **Policy**
- **Beneficiary** (para Vida)
- **Vehicle** (para Vehículo)
- **Dependent** (para Salud)

Relación base:
- Un **Client** tiene **N Policy**.

Relaciones por tipo:
- **Policy (Vida)** → 1:N **Beneficiary**
- **Policy (Vehículo)** → 1:N **Vehicle**
- **Policy (Salud)** → 1:N **Dependent**

### Reglas de integridad (sugeridas)
Dependiendo de tu implementación, es común aplicar:
- `unique(tipoDocumento, numeroDocumento)` en clientes.
- `unique(email)` en clientes (si aplica).
- `unique(policyNumber)` en pólizas (si generas número de póliza).
- `unique(plate)` en vehículos (si decides unicidad global).

---

## Arquitectura

El proyecto sigue un enfoque inspirado en **DDD (DDD-lite)**, priorizando separación de responsabilidades sin sobredimensionar la solución para una prueba técnica:

- **Domain**: entidades, enums y reglas/invariantes clave.
- **Application**: casos de uso (servicios) y DTOs.
- **Infrastructure**: controladores REST, configuración, persistencia JPA y adaptadores.

Estructura típica:

```
src/main/java/com/insurance
  ├── domain
  │   ├── model
  │   └── repository        (interfaces)
  ├── application
  │   ├── service           (casos de uso)
  │   └── dto               (request/response)
  ├── infrastructure
  │   ├── rest              (controllers)
  │   ├── persistence        (repositorios JPA / mappers)
  │   └── config            (manejo de errores, OpenAPI)
  └── shared
      └── exception         (excepciones de negocio)
```

### Manejo de errores y códigos HTTP
Se aplica manejo centralizado de excepciones para responder de forma consistente:
- **200/201**: operación exitosa.
- **400**: validaciones de entrada o reglas de negocio.
- **404**: recurso no encontrado (cliente/póliza).
- **500**: error no controlado.

---

## API y documentación

La API se documenta con **OpenAPI/Swagger**.

Una vez la app esté ejecutándose, la documentación estará disponible en una de estas rutas (según tu configuración):
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/swagger-ui/index.html`

### Endpoints esperados (referencia)
> Ajusta los paths a los que realmente tengas en tu código; dejo una guía típica.

**Clientes**
- `POST   /api/v1/clients`
- `GET    /api/v1/clients/{id}`
- `PUT    /api/v1/clients/{id}`
- `DELETE /api/v1/clients/{id}`
- `GET    /api/v1/clients`

**Pólizas**
- `POST /api/v1/policies`
- `GET  /api/v1/policies/{id}`
- `GET  /api/v1/policies/client/{clientId}`

**Salud (dependientes)**
- `GET /api/v1/policies/{policyId}/dependents`

---

## Ejecución local

### Requisitos
- Java 17+
- Maven 3.8+
- Git (opcional)

### Clonar el repositorio
```bash
git clone https://github.com/diegoalbanovoa/insurance-policy-service.git
cd insurance-policy-service
```

### Compilar
```bash
mvn clean install
```

### Ejecutar
```bash
mvn spring-boot:run
```

La aplicación queda disponible en:
- `http://localhost:8080`

> Si usas H2 en memoria, los datos se reinician en cada ejecución.

---

## Ejemplos de API

A continuación se muestran ejemplos ordenados de cómo usar todos los endpoints del sistema. Se recomienda ejecutarlos en este orden para probar el flujo completo.

### 1. Registro de usuario

Crea una nueva cuenta de usuario que será utilizada para autenticar futuras peticiones.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan.perez@insurance.com",
    "password": "SecurePass123!",
    "confirmPassword": "SecurePass123!",
    "fullName": "Juan Pérez García"
  }'
```

**Respuesta (201 Created):**
```json
{
  "message": "Usuario registrado exitosamente. Puede iniciar sesión inmediatamente.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "username": "juan.perez@insurance.com",
    "email": "juan.perez@insurance.com"
  }
}
```

**Nota importante:** Guardar el `accessToken` para usarlo en futuras peticiones en el header `Authorization: Bearer {accessToken}`.

### 2. Login

Autentica un usuario existente usando email y contraseña.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan.perez@insurance.com",
    "password": "SecurePass123!"
  }'
```

**Respuesta (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "juan.perez@insurance.com",
  "email": "juan.perez@insurance.com"
}
```

### 3. Renovar token

Obtiene un nuevo `accessToken` usando el `refreshToken` (válido 7 días sin necesidad de volver a fazer login).

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
  }'
```

### 4. Validar token

Verifica si un JWT token es válido y no ha expirado.

**curl:**
```bash
curl -X GET "http://localhost:8080/api/v1/auth/validate?token=eyJhbGciOiJIUzUxMiJ9..."
```

**Respuesta:**
```
true
```

### 5. Crear cliente

Registra un nuevo cliente en el sistema. **Requiere autenticación JWT**.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/clients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "tipoDocumento": "CC",
    "numeroDocumento": "1234567890",
    "nombres": "Juan",
    "apellidos": "Pérez García",
    "email": "juan.perez@insurance.com",
    "telefono": "+573201234567",
    "fechaNacimiento": "1990-05-15"
  }'
```

**Respuesta (201 Created):**
```json
{
  "id": 1,
  "tipoDocumento": "CC",
  "numeroDocumento": "1234567890",
  "nombres": "Juan",
  "apellidos": "Pérez García",
  "email": "juan.perez@insurance.com",
  "telefono": "+573201234567",
  "fechaNacimiento": "1990-05-15",
  "fullName": "Juan Pérez García",
  "createdAt": "2026-02-28",
  "updatedAt": "2026-02-28"
}
```

### 6. Obtener cliente por ID

Recupera los datos de un cliente específico.

**curl:**
```bash
curl -X GET http://localhost:8080/api/v1/clients/1 \
  -H "Authorization: Bearer {accessToken}"
```

### 7. Listar todos los clientes

Obtiene la lista completa de clientes registrados.

**curl:**
```bash
curl -X GET http://localhost:8080/api/v1/clients \
  -H "Authorization: Bearer {accessToken}"
```

### 8. Buscar cliente por documento

Encuentra un cliente usando su tipo y número de documento.

**curl:**
```bash
curl -X GET "http://localhost:8080/api/v1/clients/document/CC/1234567890" \
  -H "Authorization: Bearer {accessToken}"
```

### 9. Actualizar cliente

Modifica los datos de un cliente existente.

**curl:**
```bash
curl -X PUT http://localhost:8080/api/v1/clients/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "tipoDocumento": "CC",
    "numeroDocumento": "1234567890",
    "nombres": "Juan",
    "apellidos": "Pérez García",
    "email": "juan.perez.actualizado@insurance.com",
    "telefono": "+573209876543",
    "fechaNacimiento": "1990-05-15"
  }'
```

### 10. Crear póliza de Vida

Crea una póliza de vida para un cliente. **Un cliente puede tener máximo 1 póliza de vida**.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/policies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "clientId": 1,
    "policyType": "VIDA",
    "startDate": "2026-03-01",
    "endDate": "2027-03-01",
    "premiumAmount": 500000.00,
    "status": "ACTIVA"
  }'
```

**Respuesta (201 Created):**
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

### 11. Agregar beneficiario a póliza de Vida

Añade un beneficiario a una póliza de vida. **Máximo 2 beneficiarios por póliza**.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/policies/1/beneficiaries \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "fullName": "María García López",
    "documentNumber": "9876543210",
    "relationship": "Cónyuge",
    "benefitPercentage": 50.0
  }'
```

### 12. Crear póliza de Vehículo

Crea una póliza de vehículo para asegurar uno o más vehículos.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/policies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "clientId": 1,
    "policyType": "VEHICULO",
    "startDate": "2026-03-01",
    "endDate": "2027-03-01",
    "premiumAmount": 1200000.00,
    "status": "ACTIVA"
  }'
```

### 13. Agregar vehículo a póliza

Añade un vehículo a una póliza de vehículos.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/policies/2/vehicles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "brand": "Toyota",
    "model": "Corolla",
    "year": 2020,
    "plate": "ABC-1234",
    "vehicleType": "Sedán",
    "engineCC": 2000
  }'
```

### 14. Crear póliza de Salud

Crea una póliza de salud que puede cubrir al cliente y sus dependientes.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/policies \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "clientId": 1,
    "policyType": "SALUD",
    "startDate": "2026-03-01",
    "endDate": "2027-03-01",
    "premiumAmount": 800000.00,
    "status": "ACTIVA"
  }'
```

### 15. Agregar dependiente a póliza de Salud

Añade un dependiente a una póliza de salud (puede ser pareja, hijo, padre, madre, etc.).

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/policies/3/dependents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {accessToken}" \
  -d '{
    "fullName": "Pedro Pérez García",
    "relationshipType": "Hijo",
    "dateOfBirth": "2010-06-20",
    "dependentPercentage": 25.0
  }'
```

### 16. Obtener póliza por ID

Recupera los detalles completos de una póliza.

**curl:**
```bash
curl -X GET http://localhost:8080/api/v1/policies/1 \
  -H "Authorization: Bearer {accessToken}"
```

### 17. Listar pólizas de un cliente

Obtiene todas las pólizas asociadas a un cliente.

**curl:**
```bash
curl -X GET http://localhost:8080/api/v1/policies/client/1 \
  -H "Authorization: Bearer {accessToken}"
```

### 18. Listar dependientes de una póliza de Salud

Obtiene todos los dependientes cubiertos por una póliza de salud.

**curl:**
```bash
curl -X GET http://localhost:8080/api/v1/policies/3/dependents \
  -H "Authorization: Bearer {accessToken}"
```

### 19. Eliminar cliente

Elimina un cliente y todas sus pólizas asociadas (operación en cascada).

**curl:**
```bash
curl -X DELETE http://localhost:8080/api/v1/clients/1 \
  -H "Authorization: Bearer {accessToken}"
```

**Respuesta (204 No Content)**

### 20. Logout

Cierra la sesión del usuario.

**curl:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer {accessToken}"
```

**Respuesta (200 OK)**

---

## Pruebas

Para correr pruebas unitarias:

```bash
mvn test
```

Si tienes JaCoCo configurado:

```bash
mvn clean test jacoco:report
```

El reporte suele quedar en:
- `target/site/jacoco/index.html`

---

