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
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut.mongodb:micronaut-mongo-reactive")
    implementation("javax.annotation:javax.annotation-api")
    implementation(kotlin("reflect"))
    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("io.micronaut:micronaut-validation")

    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.litote.kmongo:kmongo-coroutine:4.2.8")
}

application {
    mainClass.set("dev.nycode.ApplicationKt")
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
