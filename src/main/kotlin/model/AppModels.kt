package com.example.model

import kotlinx.serialization.Serializable

// --- Subjects ---
@Serializable
data class SubjectRequest(val name: String) // Для создания/обновления

@Serializable
data class SubjectResponse(val id: Int, val name: String, val userId: Int)

// --- Questions ---
@Serializable
data class QuestionRequest(
    val title: String,
    val answer: String,
    val isLearned: Boolean? = null // null при создании, чтобы использовать default
)

@Serializable
data class QuestionResponse(
    val id: Int,
    val title: String,
    val answer: String,
    val isLearned: Boolean,
    val subjectId: Int
)

@Serializable
data class LearnedStatusUpdateRequest(val isLearned: Boolean)