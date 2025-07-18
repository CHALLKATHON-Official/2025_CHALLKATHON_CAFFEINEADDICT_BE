package com.challkathon.momento.domain.todo.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.todo.dto.response.FamilyTodoResponse
import com.challkathon.momento.domain.todo.dto.response.RecentTodoResponse
import com.challkathon.momento.domain.todo.service.FamilyTodoService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
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

        val response = familyTodoService.generateFamilyBucketList(userPrincipal.id)

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

        val response = familyTodoService.getFamilyTodosByStatus(userPrincipal.id, status)

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

        val response = familyTodoService.completeTodo(
            userPrincipal.id,
            todoId,
            image,
            memo
        )

        logger.info { "Todo completed successfully: $todoId with proof image and memo" }
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

    @Operation(
        summary = "특정 Todo 조회",
        description = "Todo ID로 특정 Todo를 조회합니다. 완료된 Todo의 경우 인증샷과 메모를 포함하여 반환합니다."
    )
    @GetMapping("/todo-lists/{todoId}")
    fun getTodo(
        @Parameter(description = "Todo ID") @PathVariable todoId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<FamilyTodoResponse>> {
        logger.info { "Getting todo: $todoId by user: ${userPrincipal.id}" }

        val response = familyTodoService.getFamilyTodo(userPrincipal.id, todoId)

        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }


    @Operation(
        summary = "최신 Todo 3개 조회",
        description = "현재 사용자가 속한 가족의 최신 Todo 3개를 조회합니다. Todo가 없으면 null을 반환합니다."
    )
    @GetMapping("/my/todo-lists/recent")
    fun getRecentTodos(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<List<RecentTodoResponse>?>> {
        logger.info { "Getting recent todos for user: ${userPrincipal.id}" }

        val response = familyTodoService.getRecentTodos(userPrincipal.id)

        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

}