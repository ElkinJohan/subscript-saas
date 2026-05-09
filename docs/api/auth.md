# Auth API

Authentication endpoints for the Subscript SaaS platform. Issues short-lived
access tokens, rotates refresh tokens with reuse detection, and revokes tokens
on logout.

All endpoints live under the `/api/auth` prefix and accept/return `application/json`.

## Token model

| Token | Lifetime | Purpose | Where it travels |
|-------|----------|---------|------------------|
| Access token  | 15 minutes | Authenticate requests to protected endpoints | `Authorization: Bearer <token>` header |
| Refresh token | 7 days     | Obtain a fresh token pair without re-login   | Request body of `/refresh` and `/logout` |

Both tokens are JWTs signed with RS256. The refresh token carries an extra
`type=refresh` claim so an access token cannot be used to refresh — defense in
depth against scope abuse.

## Endpoints

- [`POST /api/auth/login`](#post-apiauthlogin) — exchange credentials for a token pair
- [`POST /api/auth/refresh`](#post-apiauthrefresh) — rotate the token pair
- [`POST /api/auth/logout`](#post-apiauthlogout) — revoke both tokens

---

## `POST /api/auth/login`

Exchange owner credentials for a fresh token pair.

**Authentication:** none.

### Request

```json
{
  "email": "owner@example.com",
  "password": "S3cure!Pass"
}
```

| Field      | Type   | Constraints                |
|------------|--------|----------------------------|
| `email`    | string | required, valid email      |
| `password` | string | required, non-blank        |

### Responses

**`200 OK`**

```json
{
  "accessToken":  "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

**`400 Bad Request`** — request body fails validation (missing fields, malformed email).

**`401 Unauthorized`** — credentials rejected. The same response is returned for
"email not found" and "wrong password" on purpose: a discriminated message would
let an attacker enumerate valid accounts.

```json
{
  "title": "Credenciales inválidas",
  "status": 401,
  "detail": "Email o contraseña incorrectos"
}
```

### Audit

On success, emits an `AUTH_LOGIN_SUCCESS` event with the owner email.
The audit write is fire-and-forget: a failure persisting the event will not
break the login response.

### Example

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"owner@example.com","password":"S3cure!Pass"}'
```

---

## `POST /api/auth/refresh`

Trade a valid refresh token for a brand-new access + refresh pair. The
incoming refresh token is **single-use**: its `jti` is added to the Redis
blacklist (with TTL equal to its remaining lifetime) **before** the new pair
is issued. Any subsequent attempt to reuse the same refresh token is
rejected and emits an `AUTH_TOKEN_REUSE_DETECTED` audit event — a strong
signal of token theft or a buggy client.

The blacklist-then-issue ordering is deliberate: if issuance fails for any
reason, the old refresh is still revoked, so a partially failed rotation
cannot leave the old token usable.

**Authentication:** none (the refresh token in the body is the credential).

### Request

```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

| Field          | Type   | Constraints         |
|----------------|--------|---------------------|
| `refreshToken` | string | required, non-blank |

### Responses

**`200 OK`**

```json
{
  "accessToken":  "eyJhbGciOiJSUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

**`400 Bad Request`** — body fails validation.

**`401 Unauthorized`** — any of the following is collapsed into a single
generic response so the server does not leak which check failed:

- Token signature is invalid or token is malformed.
- Token has expired.
- Token is missing the `type=refresh` claim (an access token was sent).
- The owner referenced by the token no longer exists.
- **Token has already been used** (rotated and blacklisted by a prior call) —
  client-visible response is identical to the cases above on purpose, but
  server-side it is recorded as `AUTH_TOKEN_REUSE_DETECTED` (see Audit).

```json
{
  "title": "Token inválido",
  "status": 401,
  "detail": "Refresh token inválido o expirado"
}
```

### Audit

- **Success** — emits `AUTH_TOKEN_REFRESHED` with the owner email.
- **Reuse detected** — emits `AUTH_TOKEN_REUSE_DETECTED` with the offending
  `jti` and the subject claim (owner id) extracted from the rejected token.
  This event fires **before** the 401 is returned, so the audit trail is
  consistent even if the client retries.

### Example

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGciOiJSUzI1NiIs..."}'
```

---

## `POST /api/auth/logout`

Revoke both the current access token (taken from the `Authorization` header)
and the refresh token supplied in the body. Each token is added to a Redis
blacklist with a TTL equal to its remaining lifetime, so Redis evicts the
entry automatically when the JWT would have expired anyway.

**Authentication:** required — `Authorization: Bearer <accessToken>` header.

### Request

```json
{
  "refreshToken": "eyJhbGciOiJSUzI1NiIs..."
}
```

| Field          | Type   | Constraints         |
|----------------|--------|---------------------|
| `refreshToken` | string | required, non-blank |

### Responses

**`204 No Content`** — both tokens have been blacklisted.

**`400 Bad Request`** — body fails validation.

**`401 Unauthorized`** — access token missing, expired, malformed, or already
blacklisted.

### Audit

Emits an `AUTH_LOGOUT` event tied to the owner extracted from the access token.

### Example

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIs..." \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"eyJhbGciOiJSUzI1NiIs..."}'
```

---

## Security notes

- **Generic 401 responses.** Login and refresh both return a single error
  shape regardless of which check failed. This blocks user enumeration and
  token-introspection attacks.
- **Refresh-only claim.** Refresh tokens carry `type=refresh`. Access tokens
  cannot be used to refresh, even if an attacker captures one.
- **Single-use refresh + reuse detection.** Refresh tokens are blacklisted
  the moment they are consumed. A second use is treated as a security
  signal (stolen token, replayed request, broken client) and recorded as
  `AUTH_TOKEN_REUSE_DETECTED`, while the client sees the same generic 401
  as any other invalid-token case.
- **Blacklist TTL = token TTL.** Blacklisted tokens are stored in Redis only
  until their natural expiry, so the blacklist never grows unbounded.
- **Audit is fire-and-forget.** Auth events are persisted to MongoDB but a
  Mongo outage does not impact the auth flow — the audit write is wrapped in
  `onErrorResume(_ -> Mono.empty())` and a warning is logged for SRE pickup.
- **Audit trail.** Every successful login, refresh, and logout is recorded.
  `AUTH_LOGIN_FAILED` is reserved in the event-type enum and will be wired
  in once rate-limiting / brute-force detection is added.
