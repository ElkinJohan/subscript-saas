package com.ej.subscript.infrastructure.security;

import com.ej.subscript.domain.model.Owner;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Genera tokens JWT firmados con la clave privada RSA del servidor.
 *
 * <h3>Access token</h3>
 * Vida útil: 15 minutos. Contiene el ID del owner como {@code sub} y su email.
 * El cliente lo envía en cada request: {@code Authorization: Bearer <token>}.
 *
 * <h3>Refresh token</h3>
 * Vida útil: 7 días. Permite obtener un nuevo access token sin volver a autenticarse.
 * El claim {@code type=refresh} lo diferencia del access token para evitar
 * que se use como token de acceso a la API.
 */
@Component
@RequiredArgsConstructor
public class JwtService {

    private static final String ISSUER = "subscript-saas";
    private static final long ACCESS_TOKEN_MINUTES = 15;
    private static final long REFRESH_TOKEN_DAYS = 7;

    private final JwtEncoder jwtEncoder;

    /**
     * Genera un access token JWT para el owner autenticado.
     */
    public String generateAccessToken(Owner owner) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(ACCESS_TOKEN_MINUTES, ChronoUnit.MINUTES))
                .subject(owner.id().toString())
                .claim("email", owner.email())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Genera un refresh token de larga duración para el owner autenticado.
     */
    public String generateRefreshToken(Owner owner) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS))
                .subject(owner.id().toString())
                .claim("type", "refresh")
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
