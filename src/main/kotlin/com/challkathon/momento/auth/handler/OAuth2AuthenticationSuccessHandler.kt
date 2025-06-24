package com.challkathon.momento.auth.handler

import com.challkathon.momento.auth.provider.JwtProvider
import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.auth.util.TokenCookieUtil
import com.challkathon.momento.domain.user.repository.UserRepository
import io.ktor.http.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.slf4j.LoggerFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

private val log = KotlinLogging.logger { }

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
//            tokenCookieUtil.setTokens(response, accessToken, refreshToken)
            response.addHeader(
                HttpHeaders.SetCookie,
                tokenCookieUtil.createCookie("ACCESS_TOKEN", accessToken).toString()
            )
            response.addHeader(
                HttpHeaders.SetCookie,
                tokenCookieUtil.createCookie("REFRESH_TOKEN", refreshToken).toString()
            )
//            response.addHeader("Authorization", "Bearer $accessToken")

            log.info("여기까지 됐당 웅헿ㅎㅎ")
            // 프론트엔드로 단순 리다이렉트 (프로덕션에서는 실제 프론트엔드 도메인 사용)
            val targetUrl = "http://localhost:3000/oauth2/redirect"

            log.info("여기가 문제인가")
            if (response.isCommitted) {
                log.debug("응답이 이미 커밋되었습니다. $targetUrl 로 리다이렉트할 수 없습니다.")
                return
            }
            log.info("dfkjdlkfjskdfjslfkjdlskjfdk")

            clearOAuth2AuthenticationAttributes(request)

            log.info("ddddddd")
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