package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
fun main() {
    // Используем CIO, порт 8080, хост 0.0.0.0
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Конфигурируем плагины
    configureSerialization() // Обязательно
    configureStatusPages()   // Желательно
    configureRouting()       // Обязательно
}