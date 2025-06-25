package com.challkathon.momento.domain.question.controller

import com.challkathon.momento.domain.question.dto.response.FamilyQuestionListResponse
import com.challkathon.momento.domain.question.dto.response.FamilyQuestionAnswersResponse
import com.challkathon.momento.domain.question.service.FamilyQuestionService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.web.bind.annotation.*

@Tag(name = "가족 질문 조회", description = "가족에게 할당된 질문 목록 및 답변 조회 API")
@RestController
@RequestMapping("/api/v1/family/questions")
class FamilyQuestionController(
    private val familyQuestionService: FamilyQuestionService
) {

    @Operation(
        summary = "가족 질문 목록 조회",
        description = "가족에게 할당된 모든 질문 목록을 조회합니다. 각 질문의 인덱스, 내용, 모든 가족 구성원의 답변 완료 여부를 포함합니다."
    )
    @GetMapping
    fun getFamilyQuestions(
        @Parameter(hidden = true)
        @AuthenticationPrincipal userPrincipal: OAuth2User
    ): BaseResponse<FamilyQuestionListResponse> {
        val userId = userPrincipal.name.toLong()
        val response = familyQuestionService.getAllFamilyQuestions(userId)
        
        return BaseResponse.onSuccess(response)
    }

    @Operation(
        summary = "특정 질문의 가족 답변 조회",
        description = "특정 질문에 대한 모든 가족 구성원의 답변을 조회합니다. 답변하지 않은 구성원의 정보도 포함됩니다."
    )
    @GetMapping("/{familyQuestionId}/answers")
    fun getFamilyQuestionAnswers(
        @Parameter(description = "가족 질문 ID")
        @PathVariable familyQuestionId: Long,
        @Parameter(hidden = true)
        @AuthenticationPrincipal userPrincipal: OAuth2User
    ): BaseResponse<FamilyQuestionAnswersResponse> {
        val userId = userPrincipal.name.toLong()
        val response = familyQuestionService.getFamilyQuestionAnswers(userId, familyQuestionId)
        
        return BaseResponse.onSuccess(response)
    }
}