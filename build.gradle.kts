val ktorVersion = "2.3.10"
val kotlinVersion = "1.9.23"
val logbackVersion = "1.3.14"
val serializationVersion = "1.6.3"
val exposedVersion = "0.49.0"
val postgresqlVersion = "42.7.3"
val jbcryptVersion = "0.4"

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.plugin.serialization)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "com.example.ApplicationKt"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.cio)
    implementation(libs.logback.classic)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    // Ktor Core & Engine (CIO)
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")

    // Content Negotiation & Serialization
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Status Pages (для базовой обработки ошибок)
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
}

ktor {
    development = true
}
