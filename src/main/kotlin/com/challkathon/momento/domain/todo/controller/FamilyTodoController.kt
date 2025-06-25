package com.challkathon.momento.domain.todo.controller

import com.challkathon.momento.auth.security.UserPrincipal
// Removed unused imports and DTOs - using simple List<FamilyTodoResponse>
import com.challkathon.momento.domain.todo.dto.response.FamilyTodoResponse
import com.challkathon.momento.domain.todo.entity.enums.FamilyTodoStatus
import com.challkathon.momento.domain.todo.mapping.FamilyTodoList
import com.challkathon.momento.domain.todo.service.FamilyTodoService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@Tag(name = "Family Todo", description = "가족 버킷리스트 관리 API")
@RestController
@RequestMapping("/api/v1/families")
class FamilyTodoController(
    private val familyTodoService: FamilyTodoService
) {
    private val logger = KotlinLogging.logger {}

    @Operation(summary = "가족 버킷리스트 생성", description = "AI를 통해 가족 맞춤형 버킷리스트 3개를 생성합니다. 제한 없이 언제든 생성 가능")
    @PostMapping("/my/todo-lists/generate")
    fun generateFamilyBucketList(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<List<FamilyTodoResponse>>> {
        logger.info { "Generating family bucket list for user: ${userPrincipal.id}" }
        
        val familyTodos = familyTodoService.generateFamilyBucketList(userPrincipal.id)
        val response = familyTodos.map { mapToFamilyTodoResponse(it) }
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.onSuccess(response))
    }

    @Operation(
        summary = "가족 Todo 목록 조회", 
        description = "가족의 Todo 목록을 조회합니다. status 파라미터로 필터링 가능 (completed, pending)"
    )
    @GetMapping("/my/todo-lists")
    fun getFamilyTodos(
        @Parameter(description = "상태 필터 (completed, pending)") 
        @RequestParam(required = false) status: String?,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<List<FamilyTodoResponse>>> {
        logger.info { "Getting family todos for user: ${userPrincipal.id}, status: $status" }
        
        val familyTodos = familyTodoService.getFamilyTodosByStatus(userPrincipal.id, status)
        val response = familyTodos.map { mapToFamilyTodoResponse(it) }
        
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

    @Operation(
        summary = "Todo 완료 (인증샷과 메모 필수)", 
        description = "인증샷 이미지와 메모를 포함하여 Todo를 완료 처리합니다. 최대 50MB, 이미지 파일만 업로드 가능합니다."
    )
    @PutMapping(
        "/todo-lists/{todoId}/complete",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun completeTodo(
        @Parameter(description = "Todo ID") @PathVariable todoId: Long,
        @Parameter(description = "인증샷 이미지 (JPG, PNG, GIF, WEBP, 최대 50MB, 필수)") 
        @RequestPart(required = true) image: MultipartFile,
        @Parameter(description = "완료 메모 (필수)")
        @RequestPart(required = true) memo: String,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<FamilyTodoResponse>> {
        logger.info { "Completing todo: $todoId by user: ${userPrincipal.id}" }
        
        // 메모 필수 검증
        if (memo.isBlank()) {
            throw RuntimeException("완료 메모는 필수입니다.")
        }
        
        try {
            val completedTodo = familyTodoService.completeTodo(
                userPrincipal.id, 
                todoId, 
                image, 
                memo
            )
            val response = mapToFamilyTodoResponse(completedTodo)
            
            logger.info { "Todo completed successfully: $todoId with proof image and memo" }
            return ResponseEntity.ok(BaseResponse.onSuccess(response))
        } catch (e: Exception) {
            logger.error(e) { "Failed to complete todo with proof: $todoId - ${e.message}" }
            throw e
        }
    }

    @Operation(summary = "특정 Todo 조회", description = "Todo ID로 특정 Todo를 조회합니다.")
    @GetMapping("/todo-lists/{todoId}")
    fun getTodo(
        @Parameter(description = "Todo ID") @PathVariable todoId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<FamilyTodoResponse>> {
        logger.info { "Getting todo: $todoId by user: ${userPrincipal.id}" }
        
        // TODO: 개별 Todo 조회 서비스 메서드 구현 필요
        val todo = familyTodoService.getFamilyTodos(userPrincipal.id)
            .find { it.id == todoId }
        
        if (todo == null) {
            return ResponseEntity.notFound().build()
        }
        
        val response = mapToFamilyTodoResponse(todo)
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

    private fun mapToFamilyTodoResponse(familyTodo: FamilyTodoList): FamilyTodoResponse {
        return FamilyTodoResponse(
            familyTodoId = familyTodo.id,
            content = familyTodo.todoList.content,
            category = familyTodo.todoList.category.description,
            status = familyTodo.status,
            assignedAt = familyTodo.assignedAt,
            dueDate = familyTodo.dueDate,
            completedAt = familyTodo.completedAt,
            memo = familyTodo.memo,
            imageUrl = familyTodo.imageUrl
        )
    }

    // 페이징 관련 메서드 제거됨 - 단순 List 반환으로 변경
}