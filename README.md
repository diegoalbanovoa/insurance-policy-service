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

