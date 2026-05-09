package com.ej.subscript.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3 metadata and security scheme for Subscript.
 *
 * <p>The Swagger UI is served at {@code /swagger-ui.html} and the raw spec at
 * {@code /v3/api-docs}. Both paths must be allowed in {@link SecurityConfig}.
 *
 * <p>A global JWT bearer requirement is registered, so every operation appears
 * with the "Authorize" button by default. Public endpoints (login, refresh,
 * owner registration) override this with {@code security = {}} on their
 * {@code @RouterOperation}.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI subscriptOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, jwtScheme()));
    }

    private Info apiInfo() {
        return new Info()
                .title("Subscript SaaS API")
                .description("""
                        Subscription management for small businesses with recurring billing —
                        gyms, language academies, clinics, music schools. Closes the gap between
                        managing customers in a spreadsheet and adopting an enterprise platform
                        like Stripe Billing or Chargebee.

                        **v1 portfolio scope**: Auth + Owner + Client. Plans, Subscriptions and
                        Payments are sketched but out of v1.
                        """)
                .version("v1")
                .contact(new Contact()
                        .name("Elkin Johan")
                        .url("https://github.com/ElkinJohan/subscript-saas"))
                .license(new License()
                        .name("MIT")
                        .url("https://opensource.org/licenses/MIT"));
    }

    private SecurityScheme jwtScheme() {
        return new SecurityScheme()
                .name(BEARER_SCHEME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT access token returned by POST /api/auth/login. "
                        + "Tokens are signed RSA, expire in 15 minutes, and can be refreshed "
                        + "via POST /api/auth/refresh (which rotates the refresh token).");
    }
}
