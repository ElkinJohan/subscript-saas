dependencies {
    implementation(project(":domain"))

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.assertj:assertj-core:3.25.3")
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("io.projectreactor:reactor-test:3.6.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}