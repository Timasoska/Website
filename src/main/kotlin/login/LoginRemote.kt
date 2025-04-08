package com.example.login

import com.example.cache.InMemoryCache
import com.example.cache.TokenCache
import com.example.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureLoginRouting() {
    routing {
        /*post("/login") {
            val receive = call.receive<LoginReceiveRemote>()
            if (InMemoryCache.userList.map {it.login}.contains(receive.login)){
                val token = UUID.randomUUID().toString()
                InMemoryCache.token.add(TokenCache(login = receive.login, token = token))
                call.respond(
                    LoginResponseRemote(token = token),
                    typeInfo = TODO()
                )
                return@post
            }
            call.respond(HttpStatusCode.BadRequest)
        }*/
        // Роут для регистрации
        post("/register") {
            try {
                // Получаем данные из тела запроса
                val request = call.receive<RegisterRequest>()
                // Логируем полученные данные (в реальном приложении здесь была бы валидация и сохранение)
                log.info("Received registration request: login='{}', email='{}'", request.login, request.email)

                // Генерируем простой UUID в качестве "токена"
                val fakeToken = UUID.randomUUID().toString()

                // Отправляем успешный ответ с токеном
                call.respond(HttpStatusCode.OK, AuthResponse(token = fakeToken))

            } catch (e: ContentTransformationException) {
                log.error("Bad registration request: {}", e.localizedMessage)
                call.respond(HttpStatusCode.BadRequest, "Invalid request body")
            } catch (e: Exception) {
                log.error("Registration failed: {}", e.localizedMessage, e)
                call.respond(HttpStatusCode.InternalServerError, "Something went wrong")
            }
        }

        // Роут для входа
        post("/login") {
            try {
                // Получаем данные из тела запроса
                val request = call.receive<LoginRequest>()
                // Логируем полученные данные (в реальном приложении здесь была бы проверка логина/пароля)
                log.info("Received login request: login='{}'", request.login)

                // Генерируем другой простой UUID в качестве "токена"
                val fakeToken = UUID.randomUUID().toString()

                // Отправляем успешный ответ с токеном
                call.respond(HttpStatusCode.OK, AuthResponse(token = fakeToken))

            } catch (e: ContentTransformationException) {
                log.error("Bad login request: {}", e.localizedMessage)
                call.respond(HttpStatusCode.BadRequest, "Invalid request body")
            } catch (e: Exception) {
                log.error("Login failed: {}", e.localizedMessage, e)
                call.respond(HttpStatusCode.InternalServerError, "Something went wrong")
            }
        }

    }
}