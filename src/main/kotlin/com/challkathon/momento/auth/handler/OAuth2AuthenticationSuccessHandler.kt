package com.challkathon.momento.auth.handler

import com.challkathon.momento.auth.entity.RefreshToken
import com.challkathon.momento.auth.repository.RefreshTokenRepository
import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.auth.service.JwtService
import com.challkathon.momento.auth.util.CookieUtil
import com.challkathon.momento.domain.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.time.LocalDateTime

@Component
class OAuth2AuthenticationSuccessHandler(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val cookieUtil: CookieUtil,
    @Value("\${app.oauth2.authorized-redirect-uris}")
    private val authorizedRedirectUris: String
) : SimpleUrlAuthenticationSuccessHandler() {

    private val logger = KotlinLogging.logger {}

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val targetUrl = determineTargetUrl(request, response, authentication)

        if (response.isCommitted) {
            logger.debug { "Response has already been committed. Unable to redirect to $targetUrl" }
            return
        }

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }

    override fun determineTargetUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ): String {
        val userPrincipal = authentication.principal as UserPrincipal
        val user = userRepository.findById(userPrincipal.id).orElseThrow()

        // JWT 토큰 생성
        val accessToken = jwtService.generateAccessToken(user.id, user.email)
        val refreshToken = jwtService.generateRefreshToken(user.id)

        // Refresh Token DB 저장
        val refreshTokenEntity = RefreshToken(
            user = user,
            token = refreshToken,
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(refreshTokenEntity)

        // Refresh Token 쿠키 설정
        cookieUtil.addRefreshTokenCookie(response, refreshToken)

        // 환경별 프론트엔드 URL 결정
        val frontendUrl = determineFrontendUrl(request)

        // Access Token을 쿼리 파라미터로 전달 (리다이렉트에서 헤더는 소실됨)
        return UriComponentsBuilder.fromUriString(frontendUrl)
            .queryParam("token", accessToken)
            .queryParam("loginSuccess", "true")
            .build().toUriString()
    }

    private fun determineFrontendUrl(request: HttpServletRequest): String {
        // Referer나 특정 헤더를 통해 환경 구분, 또는 환경변수 사용
        val origin = request.getHeader("Origin")
        return when {
            // localhost 환경
            origin?.contains("localhost:3000") == true -> "http://localhost:3000"
            // production 환경
            origin?.contains("dev.caffeineoverdose.shop") == true -> "https://dev.caffeineoverdose.shop"
            // vercel 운영 환경
            origin?.contains("momento-neon.vercel.app") == true -> "https://momento-neon.vercel.app"
            // 기본값 (개발)
            else -> "http://localhost:3000"
        }
    }

    private fun isAuthorizedRedirectUri(uri: String): Boolean {
        return authorizedRedirectUris.split(",").any { authorizedUri ->
            uri.startsWith(authorizedUri.trim())
        }
    }
}