package com.challkathon.momento.domain.todo.dto.response

import com.challkathon.momento.domain.todo.entity.enums.FamilyTodoStatus
import java.time.LocalDateTime

/**
 * 가족 Todo 응답 DTO
 */
data class FamilyTodoResponse(
    val familyTodoId: Long,
    val content: String,
    val category: String,
    val status: FamilyTodoStatus,
    val assignedAt: LocalDateTime,
    val dueDate: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val memo: String?,
    val imageUrl: String?
)

// 페이징 관련 DTO 제거됨 - 단순 List<FamilyTodoResponse> 사용