package com.example.model

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") { // Имя таблицы
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex() // Используем email как логин
    val passwordHash = varchar("password_hash", 255) // Хеш пароля

    override val primaryKey = PrimaryKey(id)
}