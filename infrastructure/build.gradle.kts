plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

// Spring Boot 3.2.4 BOM pins org.testcontainers:* to 1.19.7, whose transitive
// docker-java 3.3.x cannot negotiate API with modern Docker daemons (28+). Override the
// property so every testcontainers artifact (including the transitive base) ships with a
// docker-java version that supports current Docker Engine API.
extra["testcontainers.version"] = "1.21.4"

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.security:spring-security-oauth2-resource-server")
    implementation("org.springframework.security:spring-security-oauth2-jose")
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
    runtimeOnly("org.postgresql:r2dbc-postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:r2dbc")
    testImplementation("org.testcontainers:mongodb")
}