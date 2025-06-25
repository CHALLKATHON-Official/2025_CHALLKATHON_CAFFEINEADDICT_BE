package com.challkathon.momento.domain.question.dto.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 답변 생성 요청 DTO
 */
data class CreateAnswerRequest(
    @field:NotBlank(message = "답변 내용은 필수입니다.")
    @field:Size(max = 10000, message = "답변 내용은 10,000자를 초과할 수 없습니다.")
    val content: String
)