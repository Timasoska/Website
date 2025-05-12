package com.example

import com.example.DatabaseFactory.dbQuery
import com.example.model.SubjectRequest // Создадим эту модель ниже
import com.example.model.SubjectResponse // Создадим эту модель ниже
import com.example.model.SubjectsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement

class SubjectRepository {

    suspend fun createSubject(request: SubjectRequest, userId: Int): SubjectResponse? {
        return dbQuery {
            val insertStatement = SubjectsTable.insert {
                it[name] = request.name
                it[SubjectsTable.userId] = userId // Привязываем к пользователю
            }
            insertStatement.resultedValues?.singleOrNull()?.let { rowToSubjectResponse(it) }
        }
    }

    suspend fun getSubjectsByUserId(userId: Int): List<SubjectResponse> {
        return dbQuery {
            SubjectsTable.select { SubjectsTable.userId eq userId }
                .mapNotNull { rowToSubjectResponse(it) }
        }
    }

    suspend fun getSubjectByIdAndUserId(subjectId: Int, userId: Int): SubjectResponse? {
        return dbQuery {
            SubjectsTable.select { (SubjectsTable.id eq subjectId) and (SubjectsTable.userId eq userId) }
                .mapNotNull { rowToSubjectResponse(it) }
                .singleOrNull()
        }
    }

    suspend fun updateSubject(subjectId: Int, request: SubjectRequest, userId: Int): Boolean {
        return dbQuery {
            SubjectsTable.update({ (SubjectsTable.id eq subjectId) and (SubjectsTable.userId eq userId) }) {
                it[name] = request.name
            } > 0 // Возвращает true если хотя бы одна строка была обновлена
        }
    }

    suspend fun deleteSubject(subjectId: Int, userId: Int): Boolean {
        // Перед удалением предмета, нужно удалить все связанные с ним вопросы
        // или настроить каскадное удаление в БД. Пока сделаем явно:
        QuestionRepository().deleteQuestionsBySubjectId(subjectId, userId) // Убедимся, что удаляем только вопросы пользователя

        return dbQuery {
            SubjectsTable.deleteWhere { (id eq subjectId) and (SubjectsTable.userId eq userId) } > 0
        }
    }

    private fun rowToSubjectResponse(row: ResultRow): SubjectResponse {
        return SubjectResponse(
            id = row[SubjectsTable.id],
            name = row[SubjectsTable.name],
            userId = row[SubjectsTable.userId]
        )
    }
}

