package com.ej.subscript.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

// Excluimos la configuración automática de R2DBC para que no busque una BD real todavía
@SpringBootApplication(exclude = {R2dbcAutoConfiguration.class})
public class SubscriptApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubscriptApplication.class, args);
    }
}