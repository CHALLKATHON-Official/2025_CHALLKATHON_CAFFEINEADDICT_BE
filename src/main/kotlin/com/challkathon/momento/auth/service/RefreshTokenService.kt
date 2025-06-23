package com.challkathon.momento.auth.service

import com.challkathon.momento.auth.dto.response.RefreshTokenResponse
import com.challkathon.momento.auth.provider.JwtProvider
import com.challkathon.momento.auth.util.TokenCookieUtil
import com.challkathon.momento.domain.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
@Transactional
class RefreshTokenService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val tokenCookieUtil: TokenCookieUtil,
    private val customUserDetailsService: CustomUserDetailsService
) {

    /**
     * Refresh Token을 사용하여 새로운 토큰 쌍 발급 (Token Rotation)
     */
    fun refreshTokens(
        request: HttpServletRequest, 
        response: HttpServletResponse
    ): RefreshTokenResponse {
        // 쿠키에서 Refresh Token 추출
        val refreshToken = tokenCookieUtil.getRefreshTokenFromCookie(request)
            ?: throw RuntimeException("Refresh Token이 없습니다.")

        // Refresh Token 유효성 검증
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            throw RuntimeException("유효하지 않은 Refresh Token입니다.")
        }

        // 토큰에서 사용자 정보 추출
        val username = jwtProvider.extractUsername(refreshToken)
        val tokenVersion = jwtProvider.extractRefreshTokenVersion(refreshToken)
            ?: throw RuntimeException("Refresh Token 버전이 없습니다.")

        // 사용자 조회
        val user = userRepository.findByEmail(username)
            .orElseThrow { RuntimeException("사용자를 찾을 수 없습니다.") }

        // 토큰 재사용 감지
        if (jwtProvider.detectTokenReuse(refreshToken, user.refreshTokenVersion)) {
            log.warn("토큰 재사용 감지로 인한 전체 세션 무효화: 사용자 = $username")
            user.invalidateAllRefreshTokens()
            userRepository.save(user)
            throw RuntimeException("보안 위반: 모든 세션이 무효화되었습니다.")
        }

        // 토큰 버전 검증
        if (tokenVersion != user.refreshTokenVersion) {
            throw RuntimeException("토큰 버전이 일치하지 않습니다.")
        }

        // 새로운 토큰 버전으로 업데이트
        val newVersion = user.incrementRefreshTokenVersion()

        // UserDetails 로드
        val userDetails = customUserDetailsService.loadUserByUsername(username)

        // 새로운 토큰 쌍 생성 (Token Rotation)
        val tokenPair = jwtProvider.rotateTokens(userDetails, newVersion)

        // 새로운 Refresh Token을 사용자 엔티티에 저장
        user.updateRefreshToken(tokenPair.refreshToken)
        user.updateLastLogin()
        userRepository.save(user)

        // 응답에 새로운 토큰들 설정
        tokenCookieUtil.setTokens(response, tokenPair.accessToken, tokenPair.refreshToken)

        log.info("토큰 갱신 성공: 사용자 = $username, 새 버전 = $newVersion")

        return RefreshTokenResponse(
            accessToken = tokenPair.accessToken,
            expiresIn = 3600, // 1시간
            tokenType = "Bearer",
            message = "토큰이 성공적으로 갱신되었습니다."
        )
    }

    /**
     * 모든 Refresh Token 무효화 (로그아웃)
     */
    fun revokeAllTokens(username: String, response: HttpServletResponse) {
        val user = userRepository.findByEmail(username)
            .orElseThrow { RuntimeException("사용자를 찾을 수 없습니다.") }

        // 모든 Refresh Token 무효화
        user.invalidateAllRefreshTokens()
        userRepository.save(user)

        // 쿠키와 헤더에서 토큰 제거
        tokenCookieUtil.clearAllTokens(response)

        log.info("모든 토큰 무효화 완료: 사용자 = $username")
    }

    /**
     * 특정 토큰 무효화 (단일 디바이스 로그아웃)
     */
    fun revokeSingleToken(
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val refreshToken = tokenCookieUtil.getRefreshTokenFromCookie(request)
            ?: return // 토큰이 없으면 무시

        try {
            val username = jwtProvider.extractUsername(refreshToken)
            val user = userRepository.findByEmail(username).orElse(null)
                ?: return

            // 토큰 버전 증가 (현재 토큰 무효화)
            user.incrementRefreshTokenVersion()
            userRepository.save(user)

            // 클라이언트에서 토큰 제거
            tokenCookieUtil.clearAllTokens(response)

            log.info("단일 토큰 무효화 완료: 사용자 = $username")
        } catch (e: Exception) {
            log.warn("토큰 무효화 중 오류 발생: ${e.message}")
            // 에러가 발생해도 클라이언트 토큰은 제거
            tokenCookieUtil.clearAllTokens(response)
        }
    }
}