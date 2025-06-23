package com.challkathon.momento.auth.handler

import com.challkathon.momento.auth.provider.JwtProvider
import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.auth.util.TokenCookieUtil
import com.challkathon.momento.domain.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Component
class OAuth2AuthenticationSuccessHandler(
    private val tokenCookieUtil: TokenCookieUtil,
    private val jwtProvider: JwtProvider,
    private val userRepository: UserRepository
) : SimpleUrlAuthenticationSuccessHandler() {

    companion object {
        private val log = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler::class.java)
    }

    @Transactional
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        try {
            val userPrincipal = authentication.principal as UserPrincipal
            val user = userPrincipal.getUser()
            
            log.info("OAuth2 로그인 성공: 사용자 ID = ${user.id}, 이메일 = ${user.email}")

            // JWT 토큰 생성
            val accessToken = jwtProvider.generateAccessToken(userPrincipal)
            val refreshToken = jwtProvider.generateRefreshToken(user.email)

            // 리프레시 토큰 업데이트 및 로그인 시간 기록
            user.updateRefreshToken(refreshToken)
            user.updateLastLogin()
            userRepository.save(user)

            // 헤더와 쿠키에 토큰 설정
            tokenCookieUtil.setTokens(response, accessToken, refreshToken)

            // 프론트엔드로 단순 리다이렉트 (사용자 정보는 별도 API에서 조회)
            val targetUrl = "http://localhost:3000?oauth=success"
            
            if (response.isCommitted) {
                log.debug("응답이 이미 커밋되었습니다. $targetUrl 로 리다이렉트할 수 없습니다.")
                return
            }

            clearOAuth2AuthenticationAttributes(request)
            response.sendRedirect(targetUrl)
            
            log.info("OAuth2 로그인 성공 리다이렉트 완료: 사용자 ID = ${user.id}")
            
        } catch (ex: Exception) {
            log.error("OAuth2 인증 성공 처리 중 오류 발생", ex)
            throw IOException("인증 성공 처리 중 오류가 발생했습니다: ${ex.message}", ex)
        }
    }


    private fun clearOAuth2AuthenticationAttributes(request: HttpServletRequest) {
        super.clearAuthenticationAttributes(request)
        request.session?.removeAttribute("SPRING_SECURITY_CONTEXT")
    }
}