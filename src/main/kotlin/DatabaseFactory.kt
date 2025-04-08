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
        Database.connect(createHikariDataSource())
        log.info("Database connected. Initializing schema...")
        transaction { // Выполняем создание таблицы в транзакции
            SchemaUtils.create(UsersTable) // Создаст таблицу, если её нет
        }
        log.info("Schema initialized.")
    }

    private fun createHikariDataSource(): HikariDataSource {
        // --- H2 Configuration ---
        val config = HikariConfig().apply {
            driverClassName = "org.h2.Driver"
            jdbcUrl = "jdbc:h2:file:./database/auth_db;DB_CLOSE_DELAY=-1" // Файл ./database/auth_db.mv.db
            username = "sa"
            password = ""
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        /* // --- PostgreSQL Configuration (Example) ---
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            // Замени <host>, <port>, <database>, <user>, <password> на свои значения
            jdbcUrl = "jdbc:postgresql://localhost:5432/your_database?user=your_user&password=your_password"
            maximumPoolSize = 5 // Можно больше для Postgres
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        */
        return HikariDataSource(config)
    }

    // Функция для выполнения DB операций в корутинах
    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() } // Выполняем в IO Dispatcher
}