# RSA keys for JWT signing

These keys are **NOT** committed to the repo. Each developer generates them locally; production must inject them via environment variables or a secrets manager.

## Generate dev keys

From this directory:

```bash
# 1. Generate a 2048-bit RSA private key
openssl genrsa -out private.pem 2048

# 2. Derive the public key
openssl rsa -in private.pem -pubout -out public.pem
```

## Files (gitignored)

- `private.pem` — RSA private key, used by `JwtEncoder` to sign access + refresh tokens
- `public.pem` — RSA public key, used by `ReactiveJwtDecoder` to verify tokens

Loaded by `RsaKeyProperties` (`@ConfigurationProperties(prefix = "rsa")`) via `application.yml`:

```yaml
rsa:
  private-key: classpath:keys/private.pem
  public-key:  classpath:keys/public.pem
```

## Production

**Do NOT** ship `*.pem` files inside the JAR. Inject the keys at runtime:

- Read from env vars (`RSA_PRIVATE_KEY`, `RSA_PUBLIC_KEY`) and override the properties at startup, or
- Mount them as a Kubernetes Secret / Docker secret and point `rsa.private-key` to a `file:` URL.

Rotate keys periodically. If a private key is leaked, **rotate immediately** and invalidate all outstanding tokens.
