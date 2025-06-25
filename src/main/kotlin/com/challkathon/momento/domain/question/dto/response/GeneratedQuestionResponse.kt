package com.challkathon.momento.domain.question.dto.response

import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import java.time.LocalDateTime

data class GeneratedQuestionResponse(
    val id: Long,
    val content: String,
    val category: QuestionCategory,
    val isAIGenerated: Boolean,
    val generatedAt: LocalDateTime
) {
    companion object {
        fun from(question: Question): GeneratedQuestionResponse {
            return GeneratedQuestionResponse(
                id = question.id,
                content = question.content,
                category = question.category,
                isAIGenerated = question.isAIGenerated,
                generatedAt = question.createdAt
            )
        }
    }
}
