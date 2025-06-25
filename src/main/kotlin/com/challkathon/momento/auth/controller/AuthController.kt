package com.challkathon.momento.auth.controller

import com.challkathon.momento.auth.dto.response.UserInfo
import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.auth.service.AuthService
import com.challkathon.momento.auth.util.CookieUtil
import com.challkathon.momento.global.common.BaseResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
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

    @GetMapping("/me")
    fun getCurrentUser(
        @AuthenticationPrincipal userPrincipal: UserPrincipal
    ): ResponseEntity<BaseResponse<UserInfo>> {
        val user = authService.getCurrentUser(userPrincipal.id)

        val userInfo = UserInfo(
            id = user.id,
            email = user.email,
            username = user.username,
            profileImageUrl = user.profileImageUrl,
            familyRole = user.familyRole?.name,
            familyRoleSelected = user.familyRoleSelected
        )

        return ResponseEntity.ok(BaseResponse.onSuccess(userInfo))
    }
}