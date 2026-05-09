package com.ej.subscript.infrastructure.config;

import com.ej.subscript.infrastructure.security.BlacklistAwareJwtDecoder;
import com.ej.subscript.infrastructure.security.TokenBlacklist;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.oauth2.jwt.JwtEncoder;

/**
 * Configura Spring Security para WebFlux con autenticación stateless via JWT RSA.
 *
 * <h3>Rutas públicas</h3>
 * <ul>
 *   <li>{@code POST /api/owners} — registro de nuevos owners</li>
 *   <li>{@code POST /api/auth/login} — obtención del token</li>
 * </ul>
 * Todas las demás rutas requieren un {@code Authorization: Bearer <accessToken>} válido.
 *
 * <h3>Flujo de autenticación</h3>
 * <ol>
 *   <li>Cliente hace {@code POST /api/auth/login} → recibe accessToken + refreshToken</li>
 *   <li>Cada request incluye {@code Authorization: Bearer <accessToken>}</li>
 *   <li>El filtro de Spring Security valida la firma RSA y la expiración</li>
 *   <li>El handler accede al principal via {@code request.principal()}</li>
 * </ol>
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(RsaKeyProperties.class)
public class SecurityConfig {

    /**
     * Cadena de filtros principal.
     * CSRF está deshabilitado porque la API es stateless (no hay cookies de sesión).
     */
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http,
                                              ReactiveJwtDecoder jwtDecoder) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.POST, "/api/owners").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/refresh").permitAll()
                        .pathMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder)
                                .jwtAuthenticationConverter(
                                        new ReactiveJwtAuthenticationConverterAdapter(
                                                new JwtAuthenticationConverter())))
                )
                .build();
    }

    /**
     * Verifica la firma de los tokens entrantes usando la clave pública RSA
     * y consulta la {@link TokenBlacklist} para rechazar tokens revocados.
     * Solo necesita la clave pública — nunca expone la privada.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(RsaKeyProperties keys, TokenBlacklist blacklist) {
        ReactiveJwtDecoder nimbus = NimbusReactiveJwtDecoder.withPublicKey(keys.publicKey()).build();
        return new BlacklistAwareJwtDecoder(nimbus, blacklist);
    }

    /**
     * Firma los tokens salientes usando el par completo (pública + privada).
     * El {@link JwtEncoder} solo vive en el servidor — nunca sale de él.
     */
    @Bean
    public JwtEncoder jwtEncoder(RsaKeyProperties keys) {
        RSAKey rsaKey = new RSAKey.Builder(keys.publicKey())
                .privateKey(keys.privateKey())
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    /**
     * BCrypt con factor de costo 10 (balance seguridad/rendimiento).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
