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

    // Ktor Core & Engine (CIO) - Consider removing if using libs aliases consistently
    // implementation("io.ktor:ktor-server-core-jvm:$ktorVersion") // Already covered by libs.ktor.server.core?
    // implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")  // Already covered by libs.ktor.server.cio?

    // Content Negotiation & Serialization - Consider removing if using libs aliases consistently
    // implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion") // Covered by libs.ktor.server.content.negotiation?
    // implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion") // Covered by libs.ktor.serialization.kotlinx.json?
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion") // Keep this if libs alias doesn't cover it

    // Logging - Consider removing if using libs aliases consistently
    // implementation("ch.qos.logback:logback-classic:$logbackVersion") // Covered by libs.logback.classic?

    // Status Pages (для базовой обработки ошибок)
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion") // Add alias for this if desired

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion") // Add aliases for these if desired

    // HikariCP Connection Pool
    implementation("com.zaxxer:HikariCP:5.1.0") // Keep only one line for HikariCP

    // PostgreSQL Driver (Добавляем!)
    runtimeOnly("org.postgresql:postgresql:42.7.3") // Используем runtimeOnly, т.к. нужен только во время выполнения

    // BCrypt
    implementation ("at.favre.lib:bcrypt:0.4.1")
    // Тестовые зависимости (оставляем)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)


}

ktor {
    development = true
}
