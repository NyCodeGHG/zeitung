plugins {
    kotlin("jvm") version "1.5.30"
    kotlin("kapt") version "1.5.30"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.micronaut.application") version "2.0.3"
    kotlin("plugin.allopen") version "1.5.30"
}

version = "0.1"
group = "dev.nycode"

repositories {
    mavenCentral()
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("dev.nycode.*")
    }
}

dependencies {
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi:3.0.2")
    kapt("io.micronaut.security:micronaut-security-annotations")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.reactor:micronaut-reactor")
    implementation("io.micronaut.reactor:micronaut-reactor-http-client")
    implementation("io.micronaut.security:micronaut-security-jwt")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.mongodb:micronaut-mongo-reactive")
    implementation("javax.annotation:javax.annotation-api")
    implementation(kotlin("reflect"))
    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("io.micronaut:micronaut-validation")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.litote.kmongo:kmongo-coroutine:4.2.8")
    implementation("de.nycode:bcrypt:2.1.3")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.2")
}

application {
    mainClass.set("dev.nycode.Application")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}
