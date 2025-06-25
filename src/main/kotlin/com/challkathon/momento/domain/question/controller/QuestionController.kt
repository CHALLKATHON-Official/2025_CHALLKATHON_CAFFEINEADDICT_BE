package com.challkathon.momento.domain.question.controller

import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.domain.question.dto.response.GeneratedQuestionResponse
import com.challkathon.momento.domain.question.service.ChatGPTQuestionService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "Question", description = "AI 질문 생성 API")
class QuestionController(
    private val chatGPTQuestionService: ChatGPTQuestionService
) {

    @PostMapping("/generate")
    @Operation(
        summary = "맞춤형 AI 질문 생성",
        description = """
            사용자의 이전 답변 이력을 기반으로 맞춤형 질문을 생성하고 저장합니다.
            - **가족에 소속된 사용자만 질문 생성 가능**
            - 생성된 질문은 데이터베이스에 저장됩니다
            - 24시간 내 최대 5개까지 생성 가능합니다
            - 중복 질문은 자동으로 필터링됩니다
        """
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "질문 생성 성공",
            content = [Content(
                mediaType = "application/json",
                schema = Schema(
                    example = """
                    {
                        "isSuccess": true,
                        "code": "COMMON200",
                        "message": "성공",
                        "data": {
                            "id": 123,
                            "content": "오늘 가족과 함께한 시간 중 가장 행복했던 순간은 언제인가요?",
                            "category": "DAILY",
                            "isAIGenerated": true,
                            "generatedAt": "2025-01-15T10:30:00"
                        }
                    }
                """
                )
            )]
        ),
        ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 (일일 생성 한도 초과 또는 가족 미설정)"
        ),
        ApiResponse(
            responseCode = "401",
            description = "인증 실패 (JWT 토큰 없음 또는 만료)"
        ),
        ApiResponse(
            responseCode = "404",
            description = "사용자를 찾을 수 없음"
        ),
        ApiResponse(
            responseCode = "500",
            description = "AI 질문 생성 실패"
        )
    )
    fun generatePersonalizedQuestion(
        @AuthenticationPrincipal user: UserPrincipal
    ): ResponseEntity<BaseResponse<GeneratedQuestionResponse>> {
        val generatedQuestion = chatGPTQuestionService.generatePersonalizedQuestion(user.id)
        return ResponseEntity.ok(BaseResponse.onSuccess(generatedQuestion))
    }
}
