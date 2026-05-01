# Subscript SaaS

SaaS reactivo de gestión de membresías para negocios locales (gimnasios, peluquerías, barberías). Cada owner administra su catálogo de planes, sus clientes y sus suscripciones; los pagos quedan trazados como audit log.

> **Estado**: MVP en construcción. Portfolio backend senior — Java + Spring WebFlux + R2DBC + Redis + MongoDB.

## Stack

| Capa | Tecnología |
|------|------------|
| Lenguaje / Build | Java 21, Gradle (Kotlin DSL) |
| Web | Spring WebFlux + RouterFunction (functional endpoints, **no** `@RestController`) |
| Persistencia transaccional | PostgreSQL 16 vía R2DBC reactivo |
| Cache / Blacklist | Redis 7 (TTL nativo, sin cron de cleanup) |
| Audit log | MongoDB 7 (event sourcing append-only) |
| Auth | OAuth2 Resource Server + JWT firmado RSA (15 min access / 7 días refresh) |
| Tests | JUnit 5 + Reactor Test + **Testcontainers** (Postgres real) |
| CI | GitHub Actions |

## Arquitectura

Hexagonal en 3 módulos Gradle:

```
domain/         ← entidades, value objects, repository ports, excepciones de negocio
application/    ← use cases (orquestación pura, sin Spring)
infrastructure/ ← Spring Boot, R2DBC, Redis, Mongo, Web, Security, main()
```

`infrastructure` depende de `application`, `application` depende de `domain`. La dependencia nunca apunta hacia adentro desde domain.

---

## Quickstart

### 1. Levantar infraestructura

```bash
docker-compose up -d
```

Esto levanta tres containers:

- `subscript-db` — Postgres en `localhost:5432` (db `subscript`, user/pass `subscript`/`subscript`)
- `subscript-redis` — Redis en `localhost:6379` (con append-only file)
- `subscript-mongo` — Mongo en `localhost:27017`

Verificá:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

Los tres deben aparecer como `healthy` en pocos segundos.

### 2. Generar el par de claves RSA

Las claves NO están en el repo. Cada dev las genera local. Desde la raíz:

```bash
cd infrastructure/src/main/resources/keys
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
cd -
```

Producción: inyectar las claves vía variables de entorno o secret manager. Detalle en `infrastructure/src/main/resources/keys/README.md`.

### 3. Correr la app

```bash
./gradlew :infrastructure:bootRun
```

La API queda en `http://localhost:8080`.

---

## Auth flow

Auth basado en JWT firmado RSA con doble token (access corto + refresh largo) y blacklist en Redis para revocación.

### Diagrama del flujo

```
                 ┌─────────────┐
                 │ POST signup │  (público)
                 └──────┬──────┘
                        ▼
                 ┌─────────────┐
                 │ POST login  │  (público) → access (15min) + refresh (7d)
                 └──────┬──────┘
                        ▼
        ┌───────────────┴───────────────┐
        ▼                               ▼
  ┌──────────┐                   ┌─────────────┐
  │ endpoint │ ── 200 ──┐        │  POST       │
  │ con      │          │        │  refresh    │  (público, body)
  │ Bearer   │ ── 401 ──┴───────►│             │ → par nuevo
  └──────────┘   (access vencido)└─────────────┘
        │
        ▼
  ┌──────────┐
  │ POST     │ (header Bearer + body refresh)
  │ logout   │ → blacklistea AMBOS en Redis
  └──────────┘
```

**Reglas clave**:

- El access vencido **no** se reusa ni se renueva — el cliente lo descarta y usa el refresh para pedir un par nuevo.
- El server **no** refresca solo. Es responsabilidad del cliente decidir CUÁNDO llamar a `/auth/refresh` (típicamente al recibir 401).
- TTL de las keys de blacklist en Redis = tiempo restante del JWT. Cuando el JWT expira solo, Redis borra la key automáticamente. Sin cron, sin cleanup manual.

### Curls

> Todos los ejemplos usan `jq` para parsear y `EMAIL`/`PASSWORD` definidos como variables de entorno. Ajustá según tu shell.

#### Signup (público)

```bash
curl -s -X POST http://localhost:8080/api/owners \
  -H "Content-Type: application/json" \
  -d '{
    "nit": "900123456",
    "name": "Elkin Test",
    "email": "elkin@test.com",
    "phone": "3001234567",
    "businessName": "Gym Test",
    "gracePeriodDays": 3,
    "password": "password123"
  }'
```

**200** con el owner creado (incluye `id` UUID).

#### Login (público)

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"elkin@test.com","password":"password123"}'
```

**200**:
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJSUzI1NiJ9..."
}
```

#### Endpoint protegido (Bearer)

```bash
ACCESS=...   # del login

curl -s http://localhost:8080/api/owners/<owner-id> \
  -H "Authorization: Bearer $ACCESS"
```

**200** con el owner. **401** si el access expiró, está blacklisted o la firma no valida.

#### Refresh (público, sin Authorization)

```bash
REFRESH=...  # del login

curl -s -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}"
```

**200** con un par nuevo. El refresh anterior queda válido en el server hasta su expiración natural (rotación opcional, ver TODO).

> ⚠️ **No mandes Authorization header acá**. Aunque el endpoint es público, si Spring detecta un Bearer en el header lo procesa igual; un Bearer blacklisted produce 401 desde el filter chain antes de que el handler corra.

#### Logout (protegido, ambos tokens)

```bash
curl -s -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $ACCESS" \
  -H "Content-Type: application/json" \
  -d "{\"refreshToken\":\"$REFRESH\"}"
```

**204 No Content**. Ambos `jti` quedan en Redis con TTL = tiempo restante del JWT respectivo.

Verificá:

```bash
docker exec subscript-redis redis-cli KEYS 'blacklist:*'
docker exec subscript-redis redis-cli TTL  'blacklist:<jti>'
```

### Diagnóstico de 401

| Cuerpo de la respuesta | Origen | Causa típica |
|------------------------|--------|--------------|
| Vacío | Filter chain de Spring Security | Bearer ausente, expirado, mal firmado o blacklisted en endpoint que requiere auth |
| `{"title":"...","detail":"..."}` | `GlobalExceptionHandler` (handler) | Validación, refresh inválido/blacklisted, owner no encontrado |

---

## Endpoint reference

| Método | Path | Auth | Descripción |
|--------|------|------|-------------|
| `POST` | `/api/owners` | público | Registro de owner |
| `POST` | `/api/auth/login` | público | Login → par de tokens |
| `POST` | `/api/auth/refresh` | público (body) | Renovar par desde refresh |
| `POST` | `/api/auth/logout` | Bearer + body | Revocar access + refresh |
| `GET` | `/api/owners/{id}` | Bearer | Owner por id |
| `POST` | `/api/owners/{ownerId}/plans` | Bearer | Crear plan |
| `GET` | `/api/owners/{ownerId}/plans` | Bearer | Listar planes del owner |
| `PATCH` | `/api/plans/{id}/deactivate` | Bearer | Desactivar plan |
| `POST` | `/api/owners/{ownerId}/clients` | Bearer | Registrar cliente |
| `GET` | `/api/owners/{ownerId}/clients` | Bearer | Listar clientes del owner |
| `PATCH` | `/api/clients/{id}/deactivate` | Bearer | Desactivar cliente |
| `POST` | `/api/subscriptions` | Bearer | Crear suscripción |
| `GET` | `/api/subscriptions/client/{clientId}` | Bearer | Suscripciones de un cliente |
| `GET` | `/api/subscriptions/client/{clientId}/active` | Bearer | Suscripciones activas de un cliente |
| `POST` | `/api/subscriptions/{id}/cancel` | Bearer | Cancelar suscripción |
| `POST` | `/api/subscriptions/{id}/renew` | Bearer | Renovar suscripción |
| `POST` | `/api/payments` | Bearer | Registrar pago |
| `GET` | `/api/payments/subscription/{subscriptionId}` | Bearer | Pagos de una suscripción |

> Para una collection lista para usar con scripts de captura de tokens, importar `insomnia-collection.json` en Insomnia.

---

## Audit log

Cada evento de auth (login OK, login fallido, token refrescado, logout) se persiste en MongoDB como evento append-only. Esto permite trazar accesos sin acoplarlo al modelo transaccional de Postgres.

Eventos definidos en `domain/audit/AuditEventType.java`:

- `AUTH_LOGIN_SUCCESS`
- `AUTH_LOGIN_FAILED`
- `AUTH_TOKEN_REFRESHED`
- `AUTH_LOGOUT`

---

## Tests

```bash
./gradlew test
```

- **Domain**: tests unitarios puros (44 tests, sin Spring).
- **Application**: use cases con mocks de repositorios.
- **Infrastructure**: integration tests con **Testcontainers** levantando Postgres real. No mocks de DB.

---

## Decisiones de arquitectura

- **WebFlux + RouterFunction**, no `@RestController`. Endpoints funcionales, composables, testeables sin levantar contexto Spring completo.
- **Hexagonal en 3 módulos Gradle**, no en paquetes. La frontera la enforce el sistema de build, no la disciplina del dev.
- **Monolito modular**, no microservicios. Bounded contexts (auth, tenant, billing, audit) modularizables vía Strangler Fig si crece la complejidad. Un solo deploy, un solo CI.
- **Redis para blacklist con TTL = vida del JWT**. Auto-cleanup nativo, sin cron job.
- **MongoDB para audit log**, Postgres para datos transaccionales. Separación física de roles.
- **JWT RSA (asimétrico)**, no HMAC. La pública puede distribuirse a otros servicios sin comprometer la firma.

---

## TODO

- [ ] Refresh token rotation (revocar el refresh anterior al emitir uno nuevo).
- [ ] Plan cache reactivo en Redis.
- [ ] Rate limiting de `AUTH_LOGIN_FAILED` con bucket en Redis.
- [ ] Integration tests para Plan / Client / Subscription / Payment.
- [ ] Métricas (Micrometer + Prometheus).
- [ ] Migrations gestionadas (Flyway / Liquibase R2DBC).
