package com.challkathon.momento.domain.question.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.question.dto.request.CreateAnswerRequest
import com.challkathon.momento.domain.question.dto.response.AnswerListResponse
import com.challkathon.momento.domain.question.dto.response.AnswerResponse
import com.challkathon.momento.domain.question.service.AnswerService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Answer", description = "답변 관리 API")
@RestController
@RequestMapping("/api/v1")
class AnswerController(
    private val answerService: AnswerService
) {
    private val logger = KotlinLogging.logger {}

    @Operation(summary = "답변 작성", description = "가족 질문에 대한 답변을 작성합니다.")
    @PostMapping("/family-questions/{familyQuestionId}/answers")
    fun createAnswer(
        @Parameter(description = "가족 질문 ID") @PathVariable familyQuestionId: Long,
        @Valid @RequestBody request: CreateAnswerRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<AnswerResponse>> {
        logger.info { "Creating answer for familyQuestion: $familyQuestionId by user: ${userPrincipal.id}" }
        
        val response = answerService.createAnswer(familyQuestionId, userPrincipal.id, request)
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.onSuccess(response))
    }

    @Operation(summary = "가족 질문별 답변 목록 조회", description = "특정 가족 질문의 모든 답변을 조회합니다.")
    @GetMapping("/family-questions/{familyQuestionId}/answers")
    fun getAnswersByFamilyQuestion(
        @Parameter(description = "가족 질문 ID") @PathVariable familyQuestionId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<AnswerListResponse>> {
        logger.info { "Getting answers for familyQuestion: $familyQuestionId by user: ${userPrincipal.id}" }
        
        val response = answerService.getAnswersByFamilyQuestion(familyQuestionId, userPrincipal.id)
        
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }

    @Operation(summary = "특정 답변 조회", description = "답변 ID로 특정 답변을 조회합니다.")
    @GetMapping("/answers/{answerId}")
    fun getAnswer(
        @Parameter(description = "답변 ID") @PathVariable answerId: Long,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<AnswerResponse>> {
        logger.info { "Getting answer: $answerId by user: ${userPrincipal.id}" }
        
        val response = answerService.getAnswer(answerId, userPrincipal.id)
        
        return ResponseEntity.ok(BaseResponse.onSuccess(response))
    }
}