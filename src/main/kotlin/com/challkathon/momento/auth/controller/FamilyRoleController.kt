package com.challkathon.momento.auth.controller

import com.challkathon.momento.auth.dto.request.FamilyRoleSelectionRequest
import com.challkathon.momento.auth.dto.response.FamilyRoleStatusResponse
import com.challkathon.momento.auth.service.FamilyRoleService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

@Tag(name = "가족 역할 API", description = "가족 역할 선택 관련 API")
@RestController
@RequestMapping("/api/v1/family-role")
class FamilyRoleController(
    private val familyRoleService: FamilyRoleService
) {

    @Operation(summary = "가족 역할 선택", description = "새 사용자가 가족 역할을 선택합니다")
    @PostMapping("/select")
    fun selectFamilyRole(
        @Valid @RequestBody request: FamilyRoleSelectionRequest,
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BaseResponse<String>> {
        log.info { "가족 역할 선택 요청: ${userDetails.username}, 역할: ${request.familyRole}" }
        
        familyRoleService.selectFamilyRole(userDetails.username, request.familyRole)
        
        return ResponseEntity.ok(BaseResponse.onSuccess("가족 역할이 성공적으로 선택되었습니다."))
    }

    @Operation(summary = "가족 역할 선택 상태 확인", description = "현재 사용자의 가족 역할 선택 상태를 확인합니다")
    @GetMapping("/status")
    fun getFamilyRoleStatus(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<BaseResponse<FamilyRoleStatusResponse>> {
        val status = familyRoleService.getFamilyRoleStatus(userDetails.username)
        
        return ResponseEntity.ok(BaseResponse.onSuccess(status))
    }
}