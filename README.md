# Subscript SaaS

> Reactive subscription management for small businesses with recurring billing —
> gyms, language academies, clinics, music schools.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

---

## The problem it solves

Small businesses that charge recurring fees — gyms, language academies, music
schools, clinics, sports clubs, niche vertical software — typically manage
billing **by hand in a spreadsheet or WhatsApp**. The result is a familiar set
of pains:

- **Lost revenue**: due dates that nobody chased
- **Active service without payment**: the owner finds out late
- **No history**: no churn, no top defaulters, no revenue per plan
- **Human errors**: charging twice, forgetting to charge, losing receipts

### Why don't they just use Stripe Billing or Chargebee?

- They charge **cash or bank transfer**, not card. Stripe Billing is built
  around card debits — it doesn't fit "the customer walks in and pays at the
  counter".
- **Cost & complexity**: Chargebee/Recurly start at USD 99–249/month plus
  per-transaction fees. That eats the margin of a 80-member gym charging
  USD 30/month.
- **Integration effort** they don't have the technical bandwidth for.

Subscript targets the **gap in the middle**: more capable than a spreadsheet,
cheaper and simpler than an enterprise platform.

---

## What this is (and isn't)

This is a **portfolio project**, not a production product. It's a vehicle to
showcase senior-level backend decisions on a real, well-scoped domain.

What you'll find here:

- A bounded domain modeled with **DDD + Hexagonal Architecture**
- **Reactive end-to-end** — WebFlux, R2DBC, reactive Redis & MongoDB
- **Non-trivial auth**: JWT RSA, token blacklist, refresh rotation with reuse
  detection — not a `password == "1234"` toy
- **Integration tests with real infrastructure** (Testcontainers), not H2 mocks
- **Mini-ADRs** in this README explaining *why* each technology was chosen and
  what trade-offs were accepted

### v1 scope

The v1 milestone covers three aggregates end-to-end:

- **Auth** — login, refresh rotation, logout, audit log
- **Owner** — registration, lookup
- **Client** — owner's customers

`Plan`, `Subscription`, and `Payment` are sketched in the codebase but are
**out of v1**. They will be promoted as the project evolves.

---

## Tech stack

| Layer | Technology |
|-------|------------|
| Language / Build | Java 21, Gradle (Kotlin DSL) |
| Web | Spring WebFlux + `RouterFunction` (functional endpoints, **no** `@RestController`) |
| Transactional store | PostgreSQL 16 over reactive R2DBC |
| Cache / Token blacklist | Redis 7 (native TTL, no cleanup cron) |
| Audit log | MongoDB 7 (append-only event store) |
| Auth | OAuth2 Resource Server + RSA-signed JWT (15-min access / 7-day refresh, rotated on use) |
| API documentation | OpenAPI 3 + Swagger UI (springdoc) |
| Tests | JUnit 5 + Reactor Test + **Testcontainers** (real Postgres & MongoDB) |
| CI | GitHub Actions |

---

## Architecture decisions

Each entry follows a compact ADR format: **context**, **decision**,
**trade-offs**.

### ADR-1: Hexagonal architecture in 3 Gradle modules

- **Context**: I need a layered architecture with strong boundaries that I
  can't accidentally violate during refactors.
- **Decision**: Three Gradle modules — `domain`, `application`,
  `infrastructure` — with the dependency direction
  `infrastructure → application → domain`. The boundary is **enforced by the
  build system**, not by package conventions or developer discipline.
- **Trade-offs**:
  - ✅ The compiler refuses to let `domain` import Spring or any infra detail.
  - ✅ Clear seams for testing — `domain` and `application` need zero Spring
    context to run.
  - ❌ Slightly more ceremony than a single-module layout (one extra
    `build.gradle.kts` per module).

### ADR-2: WebFlux + functional endpoints (no `@RestController`)

- **Context**: The app is I/O bound (DB, Redis, Mongo). Blocking servlets
  waste threads waiting on I/O.
- **Decision**: Spring WebFlux end-to-end, with `RouterFunction` + handler
  beans instead of annotation-driven controllers.
- **Trade-offs**:
  - ✅ Higher throughput per thread under concurrent I/O load.
  - ✅ Handlers are plain methods returning `Mono` — easier to compose,
    test, and reason about than annotation magic.
  - ❌ Steeper learning curve (Reactor mental model: signals, backpressure,
    eager-vs-lazy `Mono` construction).
  - ❌ Some Spring features (e.g. `@Async`, JPA) are blocking-only and would
    need bridging.

### ADR-3: PostgreSQL via R2DBC (no JPA)

- **Context**: I need a relational store, but JPA's lazy loading and session
  semantics are inherently blocking.
- **Decision**: Spring Data R2DBC with explicit reactive repositories and
  hand-written SQL where it matters.
- **Trade-offs**:
  - ✅ Stays reactive end-to-end.
  - ✅ Forces explicit query design — no surprise N+1 from a lazy graph.
  - ❌ No `@OneToMany` / `@ManyToOne` magic — joins are explicit.
  - ❌ Smaller ecosystem than JPA/Hibernate; some tooling (e.g. mature
    migrations) is still catching up.

### ADR-4: Redis for token blacklist with native TTL

- **Context**: When a user logs out (or rotates a refresh token), the JWT is
  still cryptographically valid until its `exp` claim. I need a way to
  **revoke** valid tokens without storing every issued token forever.
- **Decision**: On revoke, write the token's `jti` (JWT ID) to Redis with a
  TTL equal to the token's remaining lifetime. The JWT decoder filter
  consults Redis on every authenticated request.
- **Trade-offs**:
  - ✅ Self-cleaning: when the JWT would have expired anyway, Redis evicts
    the key automatically — no cron, no scheduled cleanup job.
  - ✅ Memory bounded: stays small even under high logout volume.
  - ❌ Adds a Redis round-trip to every authenticated request (acceptable
    for our latency budget; mitigated by Redis being in-memory and reactive).
  - ❌ Requires Redis — one more piece of infrastructure to operate.

### ADR-5: MongoDB for the audit log

- **Context**: Auth events (login OK/fail, token refreshed, logout, reuse
  detected) must be durable, append-only, and queryable independently from
  the transactional model.
- **Decision**: A separate MongoDB instance stores audit events as documents
  with a typed `AuditEventType` enum. Postgres is never touched for audit
  reads or writes.
- **Trade-offs**:
  - ✅ Append-only is a natural fit for document storage.
  - ✅ Forensic queries don't compete with transactional load on Postgres.
  - ✅ Schema flexibility for evolving event payloads.
  - ❌ Two storage technologies to operate (Postgres + Mongo).
  - ❌ Cross-store consistency is best-effort, not transactional.

### ADR-6: RSA-signed JWT + refresh rotation with reuse detection

- **Context**: Need stateless auth (no session DB), with multi-token
  semantics (short-lived access + long-lived refresh) and the ability to
  detect token theft.
- **Decision**: JWTs are signed with **RSA** (asymmetric, RS256). On
  `/auth/refresh` the incoming refresh token is **blacklisted before** the
  new pair is issued. If the same refresh is presented twice, the second
  attempt is rejected and an `AUTH_TOKEN_REUSE_DETECTED` event is recorded.
- **Trade-offs**:
  - ✅ The public key can be distributed to other services without
    compromising signing — clean separation of concerns.
  - ✅ Reuse detection catches the canonical "stolen refresh token" attack:
    the legitimate client's request will fail second, surfacing the
    compromise.
  - ❌ RSA signing is slower than HMAC (negligible at our scale, but real).
  - ❌ Key management (generation, rotation, distribution) is more involved
    than a shared secret.

### ADR-7: Testcontainers over H2 / embedded substitutes

- **Context**: Integration tests need to exercise real database behavior —
  R2DBC drivers, schema, transactions.
- **Decision**: Tests boot real Postgres and MongoDB containers via
  Testcontainers. No H2, no Mongo embedded fork.
- **Trade-offs**:
  - ✅ Tests catch driver- and dialect-specific bugs that an embedded
    substitute would hide.
  - ✅ The same image runs locally and in CI.
  - ❌ Slower than H2 (containers boot per test class).
  - ❌ Requires Docker on every dev machine and CI runner.

---

## Project layout

```
domain/         entities, value objects, repository ports, business exceptions
application/    use cases (pure orchestration, no Spring)
infrastructure/ Spring Boot, R2DBC, Redis, Mongo, Web, Security, main()
docs/api/       per-aggregate API reference in Markdown
```

`infrastructure` depends on `application`, `application` depends on `domain`.
The dependency arrow never points inward from `domain`.

---

## Quickstart

### 1. Start the infrastructure

```bash
docker-compose up -d
```

Three containers come up:

- `subscript-db` — Postgres at `localhost:5432` (db / user / pass: `subscript`)
- `subscript-redis` — Redis at `localhost:6379` (with append-only file)
- `subscript-mongo` — MongoDB at `localhost:27017`

Verify:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}"
```

All three should show `healthy` within a few seconds.

### 2. Generate the RSA key pair

The keys are **not** committed. Each developer generates them locally:

```bash
cd infrastructure/src/main/resources/keys
openssl genrsa -out private.pem 2048
openssl rsa -in private.pem -pubout -out public.pem
cd -
```

In production, keys should be injected via environment variables or a secret
manager (AWS Secrets Manager, HashiCorp Vault, etc).

### 3. Run the app

```bash
./gradlew :infrastructure:bootRun
```

The API listens on `http://localhost:8080`.

---

## API documentation

### Swagger UI (interactive)

Once the app is running, the interactive API explorer is at:

```
http://localhost:8080/swagger-ui.html
```

The raw OpenAPI 3 spec is served at `http://localhost:8080/v3/api-docs`.

Use the **Authorize** button in Swagger UI to paste the access token from
`POST /api/auth/login` and try authenticated endpoints in-browser.

### Per-aggregate reference

In-depth Markdown documentation lives under [`docs/api/`](docs/api/):

- [`docs/api/auth.md`](docs/api/auth.md) — login, refresh (with rotation),
  logout, reuse detection
- `docs/api/owners.md` — *(in progress)*
- `docs/api/clients.md` — *(in progress)*

---

## Audit log

Auth events are persisted to MongoDB as append-only documents, decoupled from
the transactional Postgres model. Events are defined in
`domain/audit/AuditEventType.java`:

- `AUTH_LOGIN_SUCCESS`
- `AUTH_LOGIN_FAILED`
- `AUTH_TOKEN_REFRESHED`
- `AUTH_TOKEN_REUSE_DETECTED` — fired when a refresh token is presented
  after rotation; signal of token theft or a misbehaving client
- `AUTH_LOGOUT`

---

## Tests

```bash
./gradlew test
```

- **Domain** — pure unit tests, no Spring context.
- **Application** — use cases with mocked repository ports.
- **Infrastructure** — integration tests with **Testcontainers** booting real
  Postgres and MongoDB. No H2, no embedded substitutes.

---

## Roadmap

### Closing v1 (Auth + Owner + Client)

- [ ] Translate remaining Spanish Javadocs and exception messages to English
- [ ] Complete `docs/api/owners.md` and `docs/api/clients.md`
- [ ] Bump Testcontainers to 1.20.x (fixes a Docker 27 API mismatch on local)

### Beyond v1 (senior breadth)

- [ ] Promote `Plan`, `Subscription`, `Payment` aggregates with full coverage
- [ ] Reactive plan cache in Redis
- [ ] Login rate limiting with a Redis token-bucket on `AUTH_LOGIN_FAILED`
- [ ] Metrics (Micrometer + Prometheus)
- [ ] Database migrations (Flyway / Liquibase for R2DBC)
- [ ] Infrastructure as Code (Terraform or Pulumi)
- [ ] Domain events on Kafka (auth events, async audit, subscription state changes)
- [ ] Kubernetes manifests + Helm chart for local cluster deployment

---

## License

MIT (file pending).
