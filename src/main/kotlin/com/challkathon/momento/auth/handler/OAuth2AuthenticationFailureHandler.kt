package com.challkathon.momento.auth.handler

import com.challkathon.momento.auth.dto.response.OAuthFailureResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component

@Component
class OAuth2AuthenticationFailureHandler(
    private val objectMapper: ObjectMapper
) : SimpleUrlAuthenticationFailureHandler() {

    companion object {
        private val log = LoggerFactory.getLogger(OAuth2AuthenticationFailureHandler::class.java)
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        try {
            log.error("OAuth2 인증 실패: ${exception.message}", exception)

            // 프론트엔드로 리다이렉트 URL 구성
            val baseUrl = "http://localhost:3000"
            val failureResponse = OAuthFailureResponse(
                success = false,
                error = mapOf(
                    "code" to "AUTH4012",
                    "message" to "OAuth 인증에 실패했습니다.",
                    "details" to (exception.message ?: "알 수 없는 오류가 발생했습니다.")
                )
            )

            // JSON을 URL 인코딩하여 쿼리 파라미터로 전달
            val jsonString = objectMapper.writeValueAsString(failureResponse)
            val encodedError = java.net.URLEncoder.encode(jsonString, "UTF-8")
            val redirectUrl = "$baseUrl/auth/error?data=$encodedError"

            // 프론트엔드로 리다이렉트
            response.sendRedirect(redirectUrl)

            log.info("OAuth2 실패 리다이렉트 완료: $baseUrl")
            
        } catch (ex: Exception) {
            log.error("OAuth2 실패 처리 중 오류 발생", ex)
            // 기본 처리로 fallback
            response.sendRedirect("http://localhost:3000/auth/error?error=oauth_failed")
        }
    }
}