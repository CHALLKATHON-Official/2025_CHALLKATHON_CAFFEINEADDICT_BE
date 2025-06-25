package com.challkathon.momento.domain.question.dto.response

import java.time.LocalDateTime

/**
 * 답변 조회 응답 DTO
 */
data class AnswerResponse(
    val answerId: Long,
    val content: String,
    val authorName: String,
    val authorProfileImage: String?,
    val authorFamilyRole: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)