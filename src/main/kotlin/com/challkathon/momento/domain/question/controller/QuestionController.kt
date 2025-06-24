package com.challkathon.momento.domain.question.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.question.dto.response.FamilyQuestionResponse
import com.challkathon.momento.domain.question.service.FamilyQuestionService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "Question", description = "AI 질문 관련 API")
class QuestionController(
    private val familyQuestionService: FamilyQuestionService
) {
    
    @GetMapping("/today")
    @Operation(
        summary = "오늘의 가족 질문 조회",
        description = "오늘 할당된 가족 질문 목록을 조회합니다."
    )
    fun getTodayFamilyQuestions(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<List<FamilyQuestionResponse>>> {
        val questions = familyQuestionService.getTodayQuestions(userPrincipal.username)
        return ResponseEntity.ok(BaseResponse.onSuccess(questions))
    }
    
    @GetMapping("/history")
    @Operation(
        summary = "가족 질문 히스토리 조회",
        description = "지정된 기간의 가족 질문 히스토리를 조회합니다."
    )
    fun getFamilyQuestionHistory(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "시작 날짜", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @Parameter(description = "종료 날짜", required = true)
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<BaseResponse<List<FamilyQuestionResponse>>> {
        val questions = familyQuestionService.getQuestionHistory(
            userId = userPrincipal.username,
            startDate = startDate,
            endDate = endDate
        )
        return ResponseEntity.ok(BaseResponse.onSuccess(questions))
    }
    
    @PostMapping("/{questionId}/regenerate")
    @Operation(
        summary = "질문 재생성",
        description = "답변하지 않은 질문을 새로운 질문으로 재생성합니다."
    )
    fun regenerateQuestion(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        @Parameter(description = "재생성할 질문 ID", required = true)
        @PathVariable questionId: Long
    ): ResponseEntity<BaseResponse<FamilyQuestionResponse>> {
        val newQuestion = familyQuestionService.regenerateQuestion(
            userId = userPrincipal.username,
            familyQuestionId = questionId
        )
        return ResponseEntity.ok(BaseResponse.onSuccess(newQuestion))
    }
}
