package com.example.plugins

import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.* // Импортируем новые репозитории
import com.example.model.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.request.*
import org.jetbrains.exposed.sql.*

fun Application.configureRouting() {

    val userRepository = UserRepository()
    val subjectRepository = SubjectRepository() // Создаем репозитории
    val questionRepository = QuestionRepository()

    routing {
        get("/") {
            call.respondText("Server is running!")
        }

        route("/auth") {
            // Эндпоинт регистрации
            post("/register") {
                try {
                    val request = call.receive<RegisterRequest>()
                    if (request.email.isBlank() || request.password.length < 6) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid email or password (min 6 chars)."))
                        return@post
                    }
                    val userId = userRepository.createUser(request)
                    if (userId != null) {
                        call.respond(HttpStatusCode.Created, AuthResponse(success = true, message = "User registered successfully.", userId = userId))
                    } else {
                        call.respond(HttpStatusCode.Conflict, AuthResponse(success = false, message = "User with this email already exists."))
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Registration failed", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "An internal error occurred during registration."))
                }
            }

            // Эндпоинт логина
            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()

                    if (request.email.isBlank() || request.password.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Email and password are required."))
                        return@post
                    }

                    val user = userRepository.findUserByEmail(request.email)
                    if (user == null) {
                        call.respond(HttpStatusCode.Unauthorized, AuthResponse(success = false, message = "Invalid credentials."))
                        return@post
                    }

                    val passwordCheckResult = BCrypt.verifyer().verify(request.password.toCharArray(), user.passwordHash)
                    if (!passwordCheckResult.verified) {
                        call.respond(HttpStatusCode.Unauthorized, AuthResponse(success = false, message = "Invalid credentials."))
                        return@post
                    }

                    call.respond(HttpStatusCode.OK, AuthResponse(success = true, message = "Login successful.", userId = user.id))

                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Login failed", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse(message = "An internal error occurred during login."))
                }
            }
        } // end /auth

        // --- Эндпоинты для ПРЕДМЕТОВ (Subjects) ---
        // Предполагаем, что userId передается как query parameter для простоты.
        // В реальном приложении это был бы ID из JWT токена.
        route("/subjects") {
            // Получить все предметы для пользователя
            get {
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing or invalid userId parameter."))
                    return@get
                }
                try {
                    val subjects = subjectRepository.getSubjectsByUserId(userId)
                    call.respond(HttpStatusCode.OK, subjects)
                } catch (e: Exception) {
                    application.log.error("Failed to get subjects for user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error fetching subjects."))
                }
            }

            // Создать новый предмет
            post {
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                if (userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing or invalid userId parameter."))
                    return@post
                }
                try {
                    val subjectRequest = call.receive<SubjectRequest>()
                    if (subjectRequest.name.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Subject name cannot be empty."))
                        return@post
                    }
                    val newSubject = subjectRepository.createSubject(subjectRequest, userId)
                    if (newSubject != null) {
                        call.respond(HttpStatusCode.Created, newSubject)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Could not create subject."))
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Failed to create subject for user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error creating subject."))
                }
            }

            // Обновить предмет
            put("/{id}") {
                val subjectId = call.parameters["id"]?.toIntOrNull()
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()

                if (subjectId == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing subjectId or userId."))
                    return@put
                }
                try {
                    val subjectRequest = call.receive<SubjectRequest>()
                    if (subjectRequest.name.isBlank()) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Subject name cannot be empty."))
                        return@put
                    }
                    val updated = subjectRepository.updateSubject(subjectId, subjectRequest, userId)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Subject updated successfully."))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Subject not found or not owned by user."))
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Failed to update subject $subjectId for user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error updating subject."))
                }
            }

            // Удалить предмет
            delete("/{id}") {
                val subjectId = call.parameters["id"]?.toIntOrNull()
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()

                if (subjectId == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing subjectId or userId."))
                    return@delete
                }
                try {
                    val deleted = subjectRepository.deleteSubject(subjectId, userId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Subject deleted successfully."))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Subject not found or not owned by user."))
                    }
                } catch (e: Exception) {
                    application.log.error("Failed to delete subject $subjectId for user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error deleting subject."))
                }
            }
        } // end /subjects

        // --- Эндпоинты для ВОПРОСОВ (Questions) ---
        route("/questions") {
            // Получить все вопросы для конкретного предмета (принадлежащего пользователю)
            get {
                val subjectId = call.request.queryParameters["subjectId"]?.toIntOrNull()
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()

                if (subjectId == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing subjectId or userId parameter."))
                    return@get
                }
                try {
                    val questions = questionRepository.getQuestionsBySubjectId(subjectId, userId)
                    call.respond(HttpStatusCode.OK, questions)
                } catch (e: Exception) {
                    application.log.error("Failed to get questions for subject $subjectId, user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error fetching questions."))
                }
            }

            // Создать новый вопрос для предмета
            post {
                val subjectId = call.request.queryParameters["subjectId"]?.toIntOrNull()
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()
                if (subjectId == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing subjectId or userId parameter."))
                    return@post
                }
                try {
                    val questionRequest = call.receive<QuestionRequest>()
                    if (questionRequest.title.isBlank() || questionRequest.answer.isBlank()){
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Question title and answer cannot be empty."))
                        return@post
                    }
                    val newQuestion = questionRepository.createQuestion(questionRequest, subjectId, userId)
                    if (newQuestion != null) {
                        call.respond(HttpStatusCode.Created, newQuestion)
                    } else {
                        call.respond(HttpStatusCode.Forbidden, ErrorResponse("Could not create question (subject not found or not owned by user)."))
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Failed to create question for subject $subjectId, user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error creating question."))
                }
            }

            // Обновить вопрос
            put("/{id}") {
                val questionId = call.parameters["id"]?.toIntOrNull()
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()

                if (questionId == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing questionId or userId."))
                    return@put
                }
                try {
                    val questionRequest = call.receive<QuestionRequest>()
                    if (questionRequest.title.isBlank() || questionRequest.answer.isBlank()){
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse("Question title and answer cannot be empty."))
                        return@put
                    }
                    val updated = questionRepository.updateQuestion(questionId, questionRequest, userId)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Question updated successfully."))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Question not found or not owned by user."))
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Failed to update question $questionId for user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error updating question."))
                }
            }

            // Обновить статус изученности вопроса
            patch("/{id}/learned") {
                val questionId = call.parameters["id"]?.toIntOrNull()
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()

                if (questionId == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing questionId or userId."))
                    return@patch
                }
                try {
                    val learnedRequest = call.receive<LearnedStatusUpdateRequest>()
                    val updated = questionRepository.updateQuestionLearnedStatus(questionId, learnedRequest.isLearned, userId)
                    if (updated) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Question learned status updated successfully."))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Question not found or not owned by user."))
                    }
                } catch (e: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request body: ${e.localizedMessage}"))
                } catch (e: Exception) {
                    application.log.error("Failed to update learned status for question $questionId, user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error updating learned status."))
                }
            }

            // Удалить вопрос
            delete("/{id}") {
                val questionId = call.parameters["id"]?.toIntOrNull()
                val userId = call.request.queryParameters["userId"]?.toIntOrNull()

                if (questionId == null || userId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing questionId or userId."))
                    return@delete
                }
                try {
                    val deleted = questionRepository.deleteQuestion(questionId, userId)
                    if (deleted) {
                        call.respond(HttpStatusCode.OK, mapOf("message" to "Question deleted successfully."))
                    } else {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse("Question not found or not owned by user."))
                    }
                } catch (e: Exception) {
                    application.log.error("Failed to delete question $questionId for user $userId", e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Error deleting question."))
                }
            }
        } // end /questions

    } // end routing
}