package com.challkathon.momento.auth.controller

import com.challkathon.momento.auth.dto.response.RefreshTokenResponse
import com.challkathon.momento.auth.service.RefreshTokenService
import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

private val log = KotlinLogging.logger {}

@Tag(name = "인증 API", description = "카카오 OAuth2 인증 및 토큰 관리 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthV1Controller(
    private val refreshTokenService: RefreshTokenService
) {

    @Operation(summary = "OAuth2 로그인 안내", description = "카카오 OAuth2 로그인 방법을 안내합니다")
    @GetMapping("/login-info")
    fun getLoginInfo(): ResponseEntity<BaseResponse<Map<String, String>>> {
        val loginInfo = mapOf(
            "message" to "카카오 로그인만 지원됩니다",
            "loginUrl" to "/oauth2/authorization/kakao",
            "description" to "위 URL로 GET 요청을 보내 카카오 로그인을 시작하세요"
        )
        
        return ResponseEntity.ok(BaseResponse.onSuccess(loginInfo))
    }

    @Operation(
        summary = "토큰 갱신", 
        description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다. " +
                "Refresh Token은 쿠키에서 자동으로 읽어옵니다."
    )
    @PostMapping("/refresh")
    fun refreshTokens(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse<RefreshTokenResponse>> {
        log.info("토큰 갱신 요청")
        
        val refreshResponse = refreshTokenService.refreshTokens(request, response)
        
        return ResponseEntity.ok(BaseResponse.onSuccess(refreshResponse))
    }

    @Operation(
        summary = "로그아웃", 
        description = "현재 사용자의 모든 토큰을 무효화합니다"
    )
    @PostMapping("/logout")
    fun logout(
        @AuthenticationPrincipal userDetails: UserDetails,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse<String>> {
        log.info("로그아웃 요청: ${userDetails.username}")
        
        refreshTokenService.revokeAllTokens(userDetails.username, response)
        
        return ResponseEntity.ok(BaseResponse.onSuccess("로그아웃이 완료되었습니다."))
    }

    @Operation(
        summary = "단일 디바이스 로그아웃", 
        description = "현재 디바이스의 토큰만 무효화합니다"
    )
    @PostMapping("/logout/device")
    fun logoutDevice(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<BaseResponse<String>> {
        log.info("단일 디바이스 로그아웃 요청")
        
        refreshTokenService.revokeSingleToken(request, response)
        
        return ResponseEntity.ok(BaseResponse.onSuccess("디바이스 로그아웃이 완료되었습니다."))
    }
}