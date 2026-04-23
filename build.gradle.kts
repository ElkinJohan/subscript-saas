plugins {
    // Plugin de Java para habilitar el soporte de lenguaje
    java
    // Definimos la versión de Spring Boot pero no la aplicamos aún (apply false)
    // para que solo se active en el módulo de infraestructura más adelante
    id("org.springframework.boot") version "3.2.4" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.ej.subscript"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        "compileOnly"("org.projectlombok:lombok:1.18.32")
        "annotationProcessor"("org.projectlombok:lombok:1.18.32")
        "testCompileOnly"("org.projectlombok:lombok:1.18.32")
        "testAnnotationProcessor"("org.projectlombok:lombok:1.18.32")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
