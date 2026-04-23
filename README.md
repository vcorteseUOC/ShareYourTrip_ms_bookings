# ShareYourTrip_ms_bookings

Microservicio de gestión de reservas para la plataforma ShareYourTrip.

## Descripción

Este microservicio es responsable de la gestión completa de solicitudes de reserva (booking requests) en la plataforma ShareYourTrip. Implementa un flujo de aprobación donde los viajeros envían solicitudes de reserva y los anfitriones pueden aceptarlas, rechazarlas o cancelarlas.

## Características Principales

- **Gestión de solicitudes de reserva**: Creación y consulta de solicitudes
- **Flujo de aprobación**: Sistema de estados (PENDING, ACCEPTED, REJECTED, CANCELLED)
- **Gestión por roles**: Viajeros pueden crear solicitudes, anfitriones pueden aprobar/rechazar
- **Validación de origen**: Filtro que valida peticiones provenientes del API Gateway
- **Búsqueda por usuario**: Consulta de solicitudes por viajero o anfitrión

## Arquitectura

```
API Gateway (8080)
   ↓ (valida JWT + añade headers X-User-Id, X-User-Roles)
Bookings Microservice (8083)
   ↓ (valida header X-User-Id)
Procesa la petición
```

## Modelo de Datos

### Entidad BookingRequest

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | Identificador único de la solicitud |
| accommodationId | Long | ID del alojamiento |
| travelerId | Long | ID del viajero que hace la solicitud |
| hostId | Long | ID del anfitrión propietario |
| startDate | LocalDate | Fecha de inicio de la estancia |
| endDate | LocalDate | Fecha de fin de la estancia |
| guestsCount | Integer | Número de huéspedes |
| message | Text | Mensaje del viajero al anfitrión |
| status | Enum | Estado (PENDING, ACCEPTED, REJECTED, CANCELLED) |
| createdAt | LocalDateTime | Fecha de creación |
| updatedAt | LocalDateTime | Fecha de última actualización |

## Estados de Solicitud

| Estado | Descripción |
|--------|-------------|
| PENDING | Solicitud pendiente de aprobación por el anfitrión |
| ACCEPTED | Solicitud aceptada por el anfitrión |
| REJECTED | Solicitud rechazada por el anfitrión |
| CANCELLED | Solicitud cancelada por el viajero |

## Endpoints

### Crear solicitud de reserva
```http
POST /booking-requests
Content-Type: application/json
Authorization: Bearer {{token}}

{
  "accommodationId": 1,
  "travelerId": 2,
  "hostId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05",
  "guestsCount": 2,
  "message": "Looking forward to staying at your place!"
}
```

**Respuesta (201):**
```json
{
  "id": 1,
  "accommodationId": 1,
  "travelerId": 2,
  "hostId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05",
  "guestsCount": 2,
  "message": "Looking forward to staying at your place!",
  "status": "PENDING",
  "createdAt": "2024-06-15T10:00:00",
  "updatedAt": "2024-06-15T10:00:00"
}
```

### Obtener solicitud por ID
```http
GET /booking-requests/{id}
Authorization: Bearer {{token}}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "accommodationId": 1,
  "travelerId": 2,
  "hostId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05",
  "guestsCount": 2,
  "message": "Looking forward to staying at your place!",
  "status": "PENDING",
  "createdAt": "2024-06-15T10:00:00",
  "updatedAt": "2024-06-15T10:00:00"
}
```

**Respuesta de error (404):**
```json
{
  "error": "Solicitud no encontrada"
}
```

### Obtener solicitudes por viajero
```http
GET /booking-requests/traveler/{travelerId}
Authorization: Bearer {{token}}
```

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "accommodationId": 1,
    "travelerId": 2,
    "hostId": 1,
    "startDate": "2024-07-01",
    "endDate": "2024-07-05",
    "guestsCount": 2,
    "message": "Looking forward to staying at your place!",
    "status": "PENDING",
    "createdAt": "2024-06-15T10:00:00",
    "updatedAt": "2024-06-15T10:00:00"
  }
]
```

### Obtener solicitudes por anfitrión
```http
GET /booking-requests/host/{hostId}
Authorization: Bearer {{token}}
```

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "accommodationId": 1,
    "travelerId": 2,
    "hostId": 1,
    "startDate": "2024-07-01",
    "endDate": "2024-07-05",
    "guestsCount": 2,
    "message": "Looking forward to staying at your place!",
    "status": "PENDING",
    "createdAt": "2024-06-15T10:00:00",
    "updatedAt": "2024-06-15T10:00:00"
  }
]
```

### Aceptar solicitud
```http
PATCH /booking-requests/{id}/accept
Authorization: Bearer {{token}}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "accommodationId": 1,
  "travelerId": 2,
  "hostId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05",
  "guestsCount": 2,
  "message": "Looking forward to staying at your place!",
  "status": "ACCEPTED",
  "createdAt": "2024-06-15T10:00:00",
  "updatedAt": "2024-06-15T11:00:00"
}
```

### Rechazar solicitud
```http
PATCH /booking-requests/{id}/reject
Authorization: Bearer {{token}}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "accommodationId": 1,
  "travelerId": 2,
  "hostId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05",
  "guestsCount": 2,
  "message": "Looking forward to staying at your place!",
  "status": "REJECTED",
  "createdAt": "2024-06-15T10:00:00",
  "updatedAt": "2024-06-15T11:00:00"
}
```

### Cancelar solicitud
```http
PATCH /booking-requests/{id}/cancel
Authorization: Bearer {{token}}
```

**Respuesta (200):**
```json
{
  "id": 1,
  "accommodationId": 1,
  "travelerId": 2,
  "hostId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05",
  "guestsCount": 2,
  "message": "Looking forward to staying at your place!",
  "status": "CANCELLED",
  "createdAt": "2024-06-15T10:00:00",
  "updatedAt": "2024-06-15T11:00:00"
}
```

## Seguridad

### Validación de Origen

El microservicio implementa un filtro `GatewayAuthenticationFilter` que:
- Valida la presencia del header `X-User-Id` enviado por el API Gateway
- Rechaza peticiones directas sin este header (401)

### Configuración Spring Security

- Spring Security configurado con `permitAll()` (validación manual)
- CSRF deshabilitado
- Filtro `GatewayAuthenticationFilter` añadido antes de `UsernamePasswordAuthenticationFilter`

## Configuración

### application.yaml

```yaml
server:
  port: 8083
  servlet:
    context-path: /

spring:
  application:
    name: share-your-trip-bookings
  datasource:
    url: jdbc:postgresql://localhost:54320/product
    username: product
    password: product
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

## Tecnologías

- Spring Boot 4.0.5
- Spring Data JPA
- Spring Security
- Spring Boot Starter Web
- PostgreSQL
- Lombok
- Jakarta Validation API

## DTOs

### BookingRequestDto
```java
{
  "accommodationId": 1,
  "travelerId": 2,
  "hostId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05",
  "guestsCount": 2,
  "message": "Looking forward to staying at your place!"
}
```

### BookingRequestResponseDto
```java
{
  "id": 1,
  "accommodationId": 1,
  "travelerId": 2,
  "hostId": 1,
  "startDate": "2024-07-01",
  "endDate": "2024-07-05",
  "guestsCount": 2,
  "message": "Looking forward to staying at your place!",
  "status": "PENDING",
  "createdAt": "2024-06-15T10:00:00",
  "updatedAt": "2024-06-15T10:00:00"
}
```

## Flujo de Reserva

1. **Creación**: El viajero crea una solicitud de reserva (estado: PENDING)
2. **Aprobación**: El anfitrión puede aceptar (estado: ACCEPTED) o rechazar (estado: REJECTED)
3. **Cancelación**: El viajero puede cancelar la solicitud (estado: CANCELLED)
4. **Restricciones**:
   - Solo se puede aceptar/rechazar si el estado es PENDING
   - Solo se puede cancelar si el estado es PENDING o ACCEPTED

## Cómo Ejecutar

### Compilar
```bash
mvnw.cmd clean package -DskipTests
```

### Ejecutar
```bash
java -jar target/*.jar
```

El servicio estará disponible en `http://localhost:8083`

## Notas Importantes

- El microservicio debe recibir peticiones únicamente a través del API Gateway
- El flujo de aprobación permite a los anfitriones controlar qué reservas aceptar
- Las fechas de reserva deben ser válidas (startDate < endDate)
- El campo `guestsCount` no debe exceder la capacidad máxima del alojamiento
- El servicio no gestiona automáticamente las fechas disponibles del alojamiento (esto debe hacerse en el servicio de accommodations)
