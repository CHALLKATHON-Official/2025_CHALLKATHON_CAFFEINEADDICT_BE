package com.challkathon.momento.domain.question.dto.response

import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "최신 질문 응답 DTO")
data class RecentQuestionResponse(
    @Schema(description = "Family Question ID", example = "1")
    val id: Long,
    
    @Schema(description = "질문 내용", example = "오늘 가족과 함께 한 가장 특별한 순간은?")
    val content: String,
    
    @Schema(description = "질문 카테고리", example = "DAILY")
    val category: QuestionCategory,
    
    @Schema(description = "질문 상태", example = "ASSIGNED")
    val status: FamilyQuestionStatus,
    
    @Schema(description = "할당된 시간", example = "2025-01-25T14:30:00")
    val assignedAt: LocalDateTime,
    
    @Schema(description = "AI 생성 여부", example = "true")
    val isAIGenerated: Boolean
)