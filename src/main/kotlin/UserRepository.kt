package com.example

import com.example.DatabaseFactory.dbQuery
import com.example.model.UsersTable
import com.example.model.RegisterRequest
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import at.favre.lib.crypto.bcrypt.BCrypt
import org.jetbrains.exposed.sql.transactions.transaction // Often needed if dbQuery doesn't handle transactions implicitly
import org.jetbrains.exposed.sql.statements.InsertStatement // Import might be needed depending on setup


// Класс для хранения данных пользователя из БД
data class UserDbInfo(val id: Int, val email: String, val passwordHash: String)

class UserRepository {

    // Поиск пользователя по email
    suspend fun findUserByEmail(email: String): UserDbInfo? {
        return dbQuery {
            UsersTable.select { UsersTable.email eq email }
                .map { rowToUserDbInfo(it) }
                .singleOrNull()
        }
    }

    /// Создание нового пользователя
    suspend fun createUser(request: RegisterRequest): Int? { // Возвращаем ID или null при ошибке
        // Проверка на существование пользователя перед хешированием
        if (findUserByEmail(request.email) != null) {
            return null // Пользователь уже существует
        }
        // Хеширование пароля
        val passwordHash = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())

        // Use dbQuery for the database operation
        return dbQuery {
            // Reference columns using UsersTable.columnName
            val statement: InsertStatement<Number> = UsersTable.insert {
                it[UsersTable.email] = request.email // Correct: Use UsersTable.email
                it[UsersTable.passwordHash] = passwordHash // Correct: Use UsersTable.passwordHash
            }
            // Retrieve the generated ID from the result of the insert
            statement get UsersTable.id
        }
    }

    // Вспомогательная функция для маппинга строки БД в объект
    private fun rowToUserDbInfo(row: ResultRow?): UserDbInfo? {
        return row?.let {
            UserDbInfo(
                id = it[UsersTable.id],
                email = it[UsersTable.email],
                passwordHash = it[UsersTable.passwordHash]
            )
        }
    }
}