package com.ej.subscript.infrastructure.security;

import com.ej.subscript.domain.model.Owner;
import com.ej.subscript.infrastructure.config.SecurityTokenProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Issues RSA-signed JWT access and refresh tokens.
 *
 * <h3>Access token</h3>
 * Lifetime configured by {@code security.token.access-ttl} (default 15m).
 * Carries the owner id as {@code sub} and the email as a custom claim.
 * Sent on every authenticated request as {@code Authorization: Bearer <token>}.
 *
 * <h3>Refresh token</h3>
 * Lifetime configured by {@code security.token.refresh-ttl} (default 7d).
 * Carries the {@code type=refresh} claim so it cannot be mistaken for an
 * access token at the resource server. Rotated and blacklisted on every
 * call to {@code /api/auth/refresh}.
 */
@Component
@RequiredArgsConstructor
public class JwtService {

    private static final String ISSUER = "subscript-saas";

    private final JwtEncoder jwtEncoder;
    private final SecurityTokenProperties tokenProperties;

    public String generateAccessToken(Owner owner) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(tokenProperties.accessTtl()))
                .subject(owner.id().toString())
                .claim("email", owner.email())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String generateRefreshToken(Owner owner) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(tokenProperties.refreshTtl()))
                .subject(owner.id().toString())
                .claim("type", "refresh")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
