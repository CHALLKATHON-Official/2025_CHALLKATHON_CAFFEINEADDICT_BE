package com.challkathon.momento.auth.controller

import com.challkathon.momento.global.common.BaseResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@Tag(name = "인증 API", description = "카카오 OAuth2 인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
class AuthV1Controller {

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
}