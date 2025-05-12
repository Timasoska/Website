package com.example

import com.example.DatabaseFactory.dbQuery
import com.example.model.QuestionRequest
import com.example.model.QuestionResponse
import com.example.model.QuestionsTable
import com.example.model.SubjectsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq // Явный импорт для eq

class QuestionRepository {

    suspend fun createQuestion(request: QuestionRequest, subjectId: Int, userId: Int): QuestionResponse? {
        // Проверим, принадлежит ли предмет этому пользователю
        val subjectOwner = dbQuery {
            SubjectsTable
                .select(SubjectsTable.userId) // slice все еще актуален, но можно и без него, если selectAll()
                .where(SubjectsTable.id eq subjectId) // Старый синтаксис select, можно обновить
                .map { it[SubjectsTable.userId] }
                .singleOrNull()
        }
        // Можно использовать '?:' для сокращения
        if (subjectOwner != userId) return null // Пользователь не владеет этим предметом

        return dbQuery {
            val insertStatement = QuestionsTable.insert {
                it[title] = request.title
                it[answer] = request.answer
                it[isLearned] = request.isLearned ?: false // Используем '?:'
                it[QuestionsTable.subjectId] = subjectId
            }
            insertStatement.resultedValues?.singleOrNull()?.let { rowToQuestionResponse(it) }
        }
    }

    suspend fun getQuestionsBySubjectId(subjectId: Int, userId: Int): List<QuestionResponse> {
        return dbQuery {
            (QuestionsTable innerJoin SubjectsTable)
                .selectAll()
                .where((QuestionsTable.subjectId eq subjectId) and (SubjectsTable.userId eq userId)) // Старый синтаксис select
                .mapNotNull { rowToQuestionResponse(it) }
        }
    }

    suspend fun getQuestionByIdAndUserId(questionId: Int, userId: Int): QuestionResponse? {
        return dbQuery {
            (QuestionsTable innerJoin SubjectsTable)
                .selectAll().where((QuestionsTable.id eq questionId) and (SubjectsTable.userId eq userId)) // Старый синтаксис select
                .mapNotNull { rowToQuestionResponse(it) }
                .singleOrNull()
        }
    }


    suspend fun updateQuestion(questionId: Int, request: QuestionRequest, userId: Int): Boolean {
        // Проверяем, что пользователь имеет право редактировать этот вопрос (через принадлежность предмета)
        getQuestionByIdAndUserId(questionId, userId) ?: return false // Использование '?:'

        return dbQuery {
            QuestionsTable.update(where = { QuestionsTable.id eq questionId }) { // Явно указываем where
                it[title] = request.title
                it[answer] = request.answer
                if (request.isLearned != null) {
                    it[isLearned] = request.isLearned
                }
            } > 0
        }
    }

    suspend fun updateQuestionLearnedStatus(questionId: Int, isLearned: Boolean, userId: Int): Boolean {
        getQuestionByIdAndUserId(questionId, userId) ?: return false // Использование '?:'
        return dbQuery {
            QuestionsTable.update(where = { QuestionsTable.id eq questionId }) { // Явно указываем where
                it[QuestionsTable.isLearned] = isLearned
            } > 0
        }
    }

    suspend fun deleteQuestion(questionId: Int, userId: Int): Boolean {
        // Проверяем, что пользователь имеет право удалять этот вопрос
        getQuestionByIdAndUserId(questionId, userId) ?: return false // Использование '?:'

        return dbQuery {
            // Явно указываем таблицу и столбец для условия
            QuestionsTable.deleteWhere { QuestionsTable.id eq questionId } > 0
        }
    }

    internal suspend fun deleteQuestionsBySubjectId(subjectId: Int, userId: Int): Int {
        // Убедимся, что удаляем вопросы для предмета, принадлежащего пользователю
        SubjectRepository().getSubjectByIdAndUserId(subjectId, userId) ?: return 0 // Использование '?:'

        return dbQuery {
            // Явно указываем таблицу и столбец для условия
            QuestionsTable.deleteWhere { QuestionsTable.subjectId eq subjectId }
        }
    }


    private fun rowToQuestionResponse(row: ResultRow): QuestionResponse {
        return QuestionResponse(
            id = row[QuestionsTable.id],
            title = row[QuestionsTable.title],
            answer = row[QuestionsTable.answer],
            isLearned = row[QuestionsTable.isLearned],
            subjectId = row[QuestionsTable.subjectId]
        )
    }
}