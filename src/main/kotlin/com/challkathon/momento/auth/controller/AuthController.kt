package com.challkathon.momento.auth.controller

import com.challkathon.momento.auth.dto.request.SelectFamilyRoleRequest
import com.challkathon.momento.auth.dto.response.UserInfo
import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.auth.service.AuthService
import com.challkathon.momento.auth.util.CookieUtil
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@Tag(name = "Auth", description = "인증 관리 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val cookieUtil: CookieUtil
) {
    private val logger = KotlinLogging.logger {}

    @PostMapping("/refresh")
    fun refreshToken(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse<String>> {
        val (newAccessToken, newRefreshToken) = authService.refreshTokenFromCookie(request)

        // 새로운 Access Token을 Authorization 헤더에 설정
        response.setHeader("Authorization", "Bearer $newAccessToken")

        // 새로운 Refresh Token을 쿠키에 설정
        cookieUtil.addRefreshTokenCookie(response, newRefreshToken)

        return ResponseEntity.ok(
            BaseResponse.onSuccess("Token refreshed successfully")
        )
    }

    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userPrincipal: UserPrincipal,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse<String>> {
        authService.logout(userPrincipal.id)

        // Refresh Token 쿠키 삭제
        cookieUtil.deleteRefreshTokenCookie(response)

        return ResponseEntity.ok(BaseResponse.onSuccess("Logged out successfully"))
    }

    @Operation(summary = "현재 사용자 정보 조회", description = "JWT 토큰을 통해 현재 로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<UserInfo>> {
        val userInfo = authService.getCurrentUserInfo(userPrincipal.id)
        return ResponseEntity.ok(BaseResponse.onSuccess(userInfo))
    }

    @Operation(summary = "가족 역할 선택", description = "사용자의 가족 역할을 선택합니다. familyRoleSelected가 false인 경우에만 가능합니다.")
    @PutMapping("/family-role")
    fun selectFamilyRole(
        @Valid @RequestBody request: SelectFamilyRoleRequest,
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<UserInfo>> {
        logger.info { "Selecting family role for user: ${userPrincipal.id}, role: ${request.familyRole}" }
        
        val userInfo = authService.selectFamilyRole(userPrincipal.id, request)
        
        return ResponseEntity.ok(BaseResponse.onSuccess(userInfo))
    }
}