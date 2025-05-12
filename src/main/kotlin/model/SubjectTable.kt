package com.example.model

import org.jetbrains.exposed.sql.Table

object SubjectsTable : Table("subjects") { // Название таблицы
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val userId = integer("user_id") references UsersTable.id // Внешний ключ к таблице пользователей

    override val primaryKey = PrimaryKey(id)
}

object QuestionsTable : Table("questions") { // Название таблицы
    val id = integer("id").autoIncrement()
    val title = text("title") // text для более длинных названий/вопросов
    val answer = text("answer")
    val isLearned = bool("is_learned").default(false)
    val subjectId = integer("subject_id") references SubjectsTable.id // Внешний ключ к таблице предметов

    override val primaryKey = PrimaryKey(id)
}