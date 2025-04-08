package com.example.plugins

import com.example.model.AuthResponse
import com.example.model.ErrorResponse
import com.example.model.LoginRequest
import com.example.model.RegisterRequest
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*

fun Application.configureRouting() { // Не принимает UserService
    routing {
        get("/") {
            call.respondText("Timasoska Ktor Server (No DB Simulation) is running!")
        }

        // --- ЭНДПОИНТ РЕГИСТРАЦИИ (СИМУЛЯЦИЯ) ---
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                application.log.info("Received /register request: login='{}'", request.login)
                call.respond(HttpStatusCode.OK, AuthResponse(success = true, message = "Registration simulated (No DB)"))
            } catch (e: BadRequestException) { throw e }
            catch (e: Exception) {
                application.log.error("/register endpoint error", e)
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("An error occurred"))
            }
        }

        // --- ЭНДПОИНТ ВХОДА (СИМУЛЯЦИЯ) ---
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                application.log.info("Received /login request: login='{}'", request.login)
                call.respond(HttpStatusCode.OK, AuthResponse(success = true, message = "Login simulated (No DB)"))
            } catch (e: BadRequestException) { throw e }
            catch (e: Exception) {
                application.log.error("/login endpoint error", e)
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("An error occurred"))
            }
        }
    }
}