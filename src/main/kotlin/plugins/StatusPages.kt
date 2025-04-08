package com.example.plugins

import com.example.model.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, cause ->
            call.application.log.warn("Bad request: {}", cause.localizedMessage)
            val errorMessage = cause.cause?.localizedMessage ?: cause.localizedMessage ?: "Invalid request"
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format: $errorMessage"))
        }
        exception<Throwable> { call, cause ->
            // Исправлено: используем call.application.log
            call.application.log.error("Unhandled exception: {}", cause.localizedMessage, cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("An internal server error occurred."))
        }
    }
}