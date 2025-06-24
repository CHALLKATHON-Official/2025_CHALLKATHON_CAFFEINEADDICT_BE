package com.challkathon.momento.auth.controller

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import jakarta.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/api/v1/auth/debug")
class OAuth2DebugController(
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id}")
    private val kakaoClientId: String,
    @Value("\${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private val kakaoRedirectUri: String,
    @Value("\${app.oauth2.authorized-redirect-uris}")
    private val authorizedRedirectUris: String,
    @Value("\${app.cors.allowed-origins}")
    private val allowedOrigins: String
) {

    companion object {
        private val log = LoggerFactory.getLogger(OAuth2DebugController::class.java)
    }

    @GetMapping("/oauth-config")
    fun getOAuthConfig(request: HttpServletRequest): Map<String, Any> {
        log.info("OAuth 설정 디버그 요청: Origin=${request.getHeader("Origin")}")
        
        return mapOf(
            "kakaoClientId" to kakaoClientId,
            "kakaoRedirectUri" to kakaoRedirectUri,
            "authorizedRedirectUris" to authorizedRedirectUris,
            "allowedOrigins" to allowedOrigins,
            "requestOrigin" to (request.getHeader("Origin") ?: "null"),
            "requestReferer" to (request.getHeader("Referer") ?: "null"),
            "oauthStartUrl" to "/oauth2/authorization/kakao"
        )
    }

    @GetMapping("/test-oauth-start")
    fun testOAuthStart(
        request: HttpServletRequest,
        @RequestParam(defaultValue = "false") redirect: Boolean
    ): Map<String, Any> {
        log.info("OAuth 시작 테스트: Origin=${request.getHeader("Origin")}")
        
        return if (redirect) {
            log.info("OAuth 리다이렉트 시작")
            mapOf("message" to "OAuth 리다이렉트 시작됨")
        } else {
            mapOf(
                "message" to "OAuth 시작 테스트 완료",
                "oauthUrl" to "/oauth2/authorization/kakao",
                "instructions" to "브라우저에서 위 URL로 직접 이동하거나 redirect=true 파라미터로 자동 리다이렉트"
            )
        }
    }

    @GetMapping("/cross-origin-test")
    fun crossOriginTest(request: HttpServletRequest): Map<String, Any> {
        log.info("Cross-origin 테스트: Origin=${request.getHeader("Origin")}")
        
        return mapOf(
            "success" to true,
            "message" to "Cross-origin 요청이 성공적으로 처리되었습니다",
            "origin" to (request.getHeader("Origin") ?: "null"),
            "userAgent" to (request.getHeader("User-Agent") ?: "null"),
            "cookies" to (request.cookies?.map { "${it.name}=${it.value}" } ?: emptyList()),
            "timestamp" to System.currentTimeMillis()
        )
    }
}