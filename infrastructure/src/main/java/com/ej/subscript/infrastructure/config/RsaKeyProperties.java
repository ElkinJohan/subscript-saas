package com.ej.subscript.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Carga el par de claves RSA desde los archivos PEM definidos en application.yml.
 *
 * <p>Spring Security registra automáticamente un {@code ConversionService} que convierte
 * la ruta {@code classpath:keys/*.pem} al objeto Java correspondiente
 * ({@link RSAPublicKey} / {@link RSAPrivateKey}) cuando
 * {@code spring-security-oauth2-resource-server} está en el classpath.
 *
 * <p>La clave privada firma los tokens (solo el servidor la conoce).
 * La clave pública verifica las firmas (puede distribuirse).
 */
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(RSAPrivateKey privateKey, RSAPublicKey publicKey) {
}
