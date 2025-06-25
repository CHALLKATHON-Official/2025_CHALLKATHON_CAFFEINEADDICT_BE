package com.challkathon.momento.domain.question.dto.response

/**
 * 답변 목록 조회 응답 DTO
 */
data class AnswerListResponse(
    val familyQuestionId: Long,
    val questionContent: String,
    val answers: List<AnswerResponse>
)