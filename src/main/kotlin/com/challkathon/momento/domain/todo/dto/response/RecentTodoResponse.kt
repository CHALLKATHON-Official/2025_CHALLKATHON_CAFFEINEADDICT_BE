package com.challkathon.momento.domain.todo.dto.response

import com.challkathon.momento.domain.todo.entity.enums.FamilyTodoStatus
import com.challkathon.momento.domain.todo.entity.enums.TodoCategory
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "최신 Todo 응답 DTO")
data class RecentTodoResponse(
    @Schema(description = "Family Todo ID", example = "1")
    val id: Long,
    
    @Schema(description = "Todo 내용", example = "가족 여행 계획 세우기")
    val content: String,
    
    @Schema(description = "Todo 카테고리", example = "TRAVEL")
    val category: TodoCategory,
    
    @Schema(description = "Todo 상태", example = "IN_PROGRESS")
    val status: FamilyTodoStatus,
    
    @Schema(description = "할당된 시간", example = "2025-01-25T10:30:00")
    val assignedAt: LocalDateTime,
    
    @Schema(description = "AI 생성 여부", example = "true")
    val isAIGenerated: Boolean
)