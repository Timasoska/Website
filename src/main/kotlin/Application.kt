package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*

fun main() {
    embeddedServer(CIO, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}
// Функция-расширение Application, которая вызывается Ktor при старте
// Она настраивает плагины и роутинг
fun Application.module() {
    // Настраиваем плагин для сериализации/десериализации JSON
    configureSerialization()
    // Настраиваем маршруты (эндпоинты) приложения
    configureRouting()
}
