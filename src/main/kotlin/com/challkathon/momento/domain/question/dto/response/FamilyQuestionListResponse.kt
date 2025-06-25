package com.challkathon.momento.domain.question.dto.response

import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import java.time.LocalDate

/**
 * 가족 질문 목록 조회 응답 DTO
 */
data class FamilyQuestionListResponse(
    val questions: List<FamilyQuestionSummary>
)

/**
 * 가족 질문 요약 정보
 */
data class FamilyQuestionSummary(
    val index: Int,                    // 질문 인덱스 (1부터 시작)
    val familyQuestionId: Long,        // FamilyQuestion ID
    val content: String,               // 질문 내용
    val category: QuestionCategory,    // 질문 카테고리
    val assignedDate: LocalDate,       // 할당된 날짜
    val allFamilyAnswered: Boolean     // 모든 가족이 답변했는지 여부
)