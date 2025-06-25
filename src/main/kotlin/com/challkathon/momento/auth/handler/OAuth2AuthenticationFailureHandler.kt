package com.challkathon.momento.auth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    private val logger = KotlinLogging.logger {}

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        // 환경별 프론트엔드 URL 결정
        val frontendUrl = determineFrontendUrl(request)

        val targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
            .queryParam("error", "authentication_failed")
            .queryParam("message", exception.localizedMessage)
            .build().toUriString()

        logger.error { "OAuth2 authentication failed: ${exception.message}" }

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    private fun determineFrontendUrl(request: HttpServletRequest): String {
        val origin = request.getHeader("Origin")
        return when {
            // localhost 환경
            origin?.contains("localhost:3000") == true -> "http://localhost:3000"
            // 운영 환경
            origin?.contains("momento-neon.vercel.app") == true -> "https://momento-neon.vercel.app"
            // 기본값 (개발)
            else -> "http://localhost:3000"
        }
    }
}