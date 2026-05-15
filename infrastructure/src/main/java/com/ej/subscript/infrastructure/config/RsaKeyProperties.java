package com.ej.subscript.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Loads the RSA key pair from the PEM files declared in application.yml.
 *
 * <p>Spring Security registers a {@code ConversionService} that turns the
 * {@code classpath:keys/*.pem} location into the matching Java object
 * ({@link RSAPublicKey} / {@link RSAPrivateKey}) when
 * {@code spring-security-oauth2-resource-server} is on the classpath.
 *
 * <p>The private key signs tokens (kept secret by the server). The public
 * key verifies signatures (safe to distribute).
 */
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
}
