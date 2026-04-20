plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":application"))
    implementation(project(":domain"))
    // Starter para Web Reactiva (WebFlux)
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // Starter para persistencia reactiva (R2DBC)
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}