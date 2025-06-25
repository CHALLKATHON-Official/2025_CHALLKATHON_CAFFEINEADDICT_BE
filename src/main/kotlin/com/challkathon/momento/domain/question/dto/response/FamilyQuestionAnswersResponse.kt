package com.challkathon.momento.domain.question.dto.response

import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 특정 질문의 가족 답변 조회 응답 DTO
 */
data class FamilyQuestionAnswersResponse(
    val question: QuestionDetail,
    val answers: List<FamilyMemberAnswer>
)

/**
 * 질문 상세 정보
 */
data class QuestionDetail(
    val familyQuestionId: Long,
    val content: String,
    val category: QuestionCategory,
    val assignedDate: LocalDate
)

/**
 * 가족 구성원의 답변 정보
 */
data class FamilyMemberAnswer(
    val answerId: Long?,              // 답변 ID (답변하지 않은 경우 null)
    val memberName: String,           // 가족 구성원 이름
    val memberProfileImage: String?,  // 프로필 이미지 URL
    val content: String?,             // 답변 내용 (답변하지 않은 경우 null)
    val answeredAt: LocalDateTime?    // 답변 시간 (답변하지 않은 경우 null)
)