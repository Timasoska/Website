package com.example

import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.cio.EngineMain.main(args) // Используем CIO

@Suppress("unused") // Используется application.conf или EngineMain
fun Application.module() {
    DatabaseFactory.init()
    configureSerialization()
    // configureStatusPages() // Если добавил - хорошо
    configureRouting()    // Оставь только ОДИН вызов
}