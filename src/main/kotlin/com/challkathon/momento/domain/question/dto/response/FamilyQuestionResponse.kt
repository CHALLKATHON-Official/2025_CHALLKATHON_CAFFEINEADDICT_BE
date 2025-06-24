package com.challkathon.momento.domain.question.dto.response

import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import java.time.LocalDateTime

data class FamilyQuestionResponse(
    val id: Long,
    val questionId: Long,
    val content: String,
    val category: QuestionCategory,
    val assignedAt: LocalDateTime,
    val dueDate: LocalDateTime?,
    val status: FamilyQuestionStatus,
    val answerCount: Int,
    val answerRate: Double,
    val isOverdue: Boolean
) {
    companion object {
        fun from(familyQuestion: FamilyQuestion): FamilyQuestionResponse {
            return FamilyQuestionResponse(
                id = familyQuestion.id,
                questionId = familyQuestion.question.id,
                content = familyQuestion.question.content,
                category = familyQuestion.question.category,
                assignedAt = familyQuestion.assignedAt,
                dueDate = familyQuestion.dueDate,
                status = familyQuestion.status,
                answerCount = familyQuestion.answers.size,
                answerRate = familyQuestion.getAnswerRate(),
                isOverdue = familyQuestion.isOverdue()
            )
        }
    }
}
