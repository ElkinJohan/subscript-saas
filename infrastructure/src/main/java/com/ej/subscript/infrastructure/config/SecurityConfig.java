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
 * Configures Spring Security for WebFlux with stateless JWT-RSA authentication.
 *
 * <h3>Public routes</h3>
 * <ul>
 *   <li>{@code POST /api/owners} — new owner registration</li>
 *   <li>{@code POST /api/auth/login} — token issuance</li>
 * </ul>
 * Every other route requires a valid {@code Authorization: Bearer <accessToken>}.
 *
 * <h3>Authentication flow</h3>
 * <ol>
 *   <li>Client calls {@code POST /api/auth/login} → receives accessToken + refreshToken</li>
 *   <li>Each request carries {@code Authorization: Bearer <accessToken>}</li>
 *   <li>The Spring Security filter validates the RSA signature and expiration</li>
 *   <li>The handler reads the principal via {@code request.principal()}</li>
 * </ol>
 */
@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties({RsaKeyProperties.class, SecurityTokenProperties.class})
public class SecurityConfig {

    /**
     * Main filter chain.
     * CSRF is disabled because the API is stateless (no session cookies).
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
     * Verifies incoming token signatures with the RSA public key and looks
     * them up in the {@link TokenBlacklist} to reject revoked ones. Only
     * needs the public key — the private key never leaves the encoder.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder(RsaKeyProperties keys, TokenBlacklist blacklist) {
        ReactiveJwtDecoder nimbus = NimbusReactiveJwtDecoder.withPublicKey(keys.publicKey()).build();
        return new BlacklistAwareJwtDecoder(nimbus, blacklist);
    }

    /**
     * Signs outgoing tokens using the full key pair (public + private).
     * The {@link JwtEncoder} lives only on the server — never leaves it.
     */
    @Bean
    public JwtEncoder jwtEncoder(RsaKeyProperties keys) {
        RSAKey rsaKey = new RSAKey.Builder(keys.publicKey())
                .privateKey(keys.privateKey())
                .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
    }

    /**
     * BCrypt with a cost factor of 10 (security/performance balance).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
