package com.example.plugins

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.UserRepository
import com.example.model.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.request.*
import org.jetbrains.exposed.sql.* // Можно импортировать все из sql

// ... остальной код ...

fun Application.configureRouting() {

    val userRepository = UserRepository() // Создаем репозиторий

    routing {
        get("/") { // Твой корневой роут
            call.respondText("Server is running!")
        }

        route("/auth") { // Группа роутов для аутентификации
            post("/register") {
                try {
                    val request = call.receive<RegisterRequest>() // Ktor автоматически десериализует JSON

                    // Простая валидация
                    if (request.email.isBlank() || request.password.length < 6) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid email or password (min 6 chars)."))
                        return@post
                    }

                    // Пытаемся создать пользователя
                    val userId = userRepository.createUser(request)

                    if (userId != null) {
                        // Успех
                        call.respond(HttpStatusCode.Created, AuthResponse(success = true, message = "User registered successfully.", userId = userId))
                    } else {
                        // Ошибка (вероятно, пользователь уже существует)
                        call.respond(HttpStatusCode.Conflict, AuthResponse(success = false, message = "User with this email already exists."))
                    }

                } catch (e: ContentTransformationException) { // Ошибка парсинга JSON
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) { // Другие ошибки сервера
                    application.log.error("Registration failed", e) // Логируем ошибку
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "An internal error occurred during registration."))
                }
            }

            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()

                    if (request.email.isBlank() || request.password.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Email and password are required."))
                        return@post
                    }

                    // Ищем пользователя
                    val user = userRepository.findUserByEmail(request.email)
                    if (user == null) {
                        call.respond(HttpStatusCode.Unauthorized, AuthResponse(success = false, message = "Invalid credentials.")) // Не говорим, что именно не так
                        return@post
                    }

                    // Проверяем хеш пароля
                    val passwordCheckResult = BCrypt.verifyer().verify(request.password.toCharArray(), user.passwordHash)
                    if (!passwordCheckResult.verified) {
                        call.respond(HttpStatusCode.Unauthorized, AuthResponse(success = false, message = "Invalid credentials."))
                        return@post
                    }

                    // Успешный вход
                    call.respond(HttpStatusCode.OK, AuthResponse(success = true, message = "Login successful.", userId = user.id))

                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Login failed", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "An internal error occurred during login."))
                }
            }
        } // end /auth
    } // end routing
}