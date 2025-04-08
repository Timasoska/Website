package com.example

import com.example.model.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun init() {
        val db = Database.connect(createHikariDataSource())
        log.info("Database connection pool initialized. Attempting to connect and initialize schema...")
        transaction(db) { // Указываем базу данных явно для транзакции инициализации
            try {
                // Проверяем соединение (необязательный шаг, но полезный)
                exec("SELECT 1")
                log.info("Database connection successful.")
                // Создаем таблицу, если она не существует
                SchemaUtils.create(UsersTable)
                log.info("Schema initialization complete for UsersTable.")
            } catch (e: Exception) {
                log.error("Database connection or schema initialization failed!", e)
                // В зависимости от критичности, можно либо остановить приложение, либо продолжить с ошибкой
                throw e // Перевыбросить исключение, чтобы приложение не запустилось без БД
            }
        }
    }

    private fun createHikariDataSource(): HikariDataSource {
        log.info("Creating HikariDataSource for PostgreSQL...")

        // --- Конфигурация PostgreSQL ---
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            // --- ВАЖНО: Замените значения ниже на ваши реальные ---
            jdbcUrl = "jdbc:postgresql://localhost:8080/ktor_auth_db" // Укажите хост, порт и имя ВАШЕЙ БД
            username = "postgres" // Имя пользователя ВАШЕЙ БД
            password = "password" // Пароль ВАШЕГО пользователя БД (лучше выносить в переменные окружения)
            // --- Конец блока для замены ---

            maximumPoolSize = 5 // Количество соединений в пуле
            isAutoCommit = false // Рекомендуется false для Exposed
            transactionIsolation = "TRANSACTION_REPEATABLE_READ" // Стандартный уровень изоляции
            leakDetectionThreshold = 60 * 1000 // Время (мс) для обнаружения утечек соединений (опционально)

            // Валидация конфигурации HikariCP
            try {
                validate()
                log.info("HikariConfig validation successful.")
            } catch (e: IllegalStateException) {
                log.error("HikariConfig validation failed!", e)
                throw e // Останавливаем запуск, если конфиг невалиден
            }
        }

        return HikariDataSource(config)
    }

    // Функция для выполнения DB операций в корутинах (остается без изменений)
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}