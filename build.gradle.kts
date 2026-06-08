plugins {
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
    id("com.diffplug.spotless") version "6.25.0"
    id("checkstyle")
    java
}

group = "com.resilient"
version = "1.0.0"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Kafka
    implementation("org.springframework.kafka:spring-kafka")

    // Redis + Redisson (distributed lock + storage)
    implementation("org.redisson:redisson-spring-boot-starter:3.50.0")

    // Resilience4j
    implementation("io.github.resilience4j:resilience4j-spring-boot3:2.3.0")
    implementation("io.github.resilience4j:resilience4j-reactor:2.3.0")

    // Caching (reactive-safe in-process cache)
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    // JSON
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // Observability
    implementation("net.logstash.logback:logstash-logback-encoder:8.0")
    implementation("io.micrometer:micrometer-registry-prometheus")

    // OpenAPI / Swagger UI
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("io.projectreactor:reactor-test")

    // TestContainers
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.testcontainers:kafka:1.19.8")
    testImplementation("org.testcontainers:mongodb:1.19.8")
    testImplementation("org.testcontainers:testcontainers:1.19.8")

    testImplementation("org.mockito:mockito-core")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.awaitility:awaitility:4.2.2")
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

tasks.register<Test>("integrationTest") {
    description = "Runs JUnit tests tagged @Tag(\"integration\") — requires Docker."
    group = "verification"
    testClassesDirs = sourceSets["test"].output.classesDirs
    classpath = sourceSets["test"].runtimeClasspath
    useJUnitPlatform {
        includeTags("integration")
    }
    shouldRunAfter("test")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

tasks.bootJar {
    archiveFileName.set("order-worker.jar")
}

spotless {
    java {
        target("src/**/*.java")
        googleJavaFormat("1.22.0").aosp()
        importOrder("java", "javax", "org", "com", "")
        removeUnusedImports()
        formatAnnotations()
        licenseHeader(
            """
            /*
             * Copyright (c) 2025 Resilient Order Enricher
             *
             * Licensed under the MIT License.
             */
            """.trimIndent(),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint()
    }
}

checkstyle {
    toolVersion = "10.12.7"
    configFile = file("config/checkstyle/checkstyle.xml")
    configDirectory = file("config/checkstyle")
    isIgnoreFailures = false
    maxWarnings = 0
}

tasks.withType<Checkstyle> {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}
