package com.challkathon.momento.auth.service

import com.challkathon.momento.auth.dto.response.UserInfo
import com.challkathon.momento.auth.entity.RefreshToken
import com.challkathon.momento.auth.enums.TokenType
import com.challkathon.momento.auth.exception.AuthException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.auth.repository.RefreshTokenRepository
import com.challkathon.momento.auth.util.CookieUtil
import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.repository.UserRepository
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val cookieUtil: CookieUtil
) {
    private val logger = KotlinLogging.logger {}

    fun refreshTokenFromCookie(request: HttpServletRequest): Pair<String, String> {
        // 1. 쿠키에서 Refresh Token 읽기
        val refreshTokenValue = cookieUtil.getRefreshTokenFromCookie(request)
            ?: throw AuthException(AuthErrorStatus._REFRESH_TOKEN_NOT_FOUND)

        return refreshToken(refreshTokenValue)
    }

    fun refreshToken(refreshTokenValue: String): Pair<String, String> {
        // 1. Refresh Token 유효성 검증
        if (!jwtService.validateToken(refreshTokenValue)) {
            throw AuthException(AuthErrorStatus._REFRESH_TOKEN_INVALID)
        }

        // 2. Token Type 확인
        if (jwtService.getTokenType(refreshTokenValue) != TokenType.REFRESH) {
            throw AuthException(AuthErrorStatus._TOKEN_INVALID)
        }

        // 3. DB에서 Refresh Token 조회
        val refreshTokenEntity = refreshTokenRepository.findByTokenAndIsRevokedFalse(refreshTokenValue)
            .orElseThrow { AuthException(AuthErrorStatus._REFRESH_TOKEN_NOT_FOUND) }

        // 4. Refresh Token 유효성 확인
        if (!refreshTokenEntity.isValid()) {
            throw AuthException(AuthErrorStatus._REFRESH_TOKEN_EXPIRED)
        }

        val user = refreshTokenEntity.user

        // 5. 기존 Refresh Token 무효화 (Rotation)
        refreshTokenEntity.revoke()

        // 6. 새로운 토큰 생성
        val newAccessToken = jwtService.generateAccessToken(user.id, user.email)
        val newRefreshToken = jwtService.generateRefreshToken(user.id)

        // 7. 새로운 Refresh Token DB 저장
        val newRefreshTokenEntity = RefreshToken(
            user = user,
            token = newRefreshToken,
            expiresAt = LocalDateTime.now().plusDays(7)
        )
        refreshTokenRepository.save(newRefreshTokenEntity)

        logger.info { "Token refreshed for user: ${user.email}" }

        return Pair(newAccessToken, newRefreshToken)
    }

    fun logout(userId: Long) {
        val user = userRepository.findById(userId).orElse(null) ?: return
        
        // 해당 사용자의 모든 Refresh Token 무효화
        refreshTokenRepository.revokeAllByUser(user)
        
        logger.info { "User logged out: ${user.email}" }
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }
    }

    @Transactional(readOnly = true)
    fun getCurrentUserInfo(userId: Long): UserInfo {
        val user = userRepository.findById(userId)
            .orElseThrow { AuthException(AuthErrorStatus._USER_NOT_FOUND) }
        
        return UserInfo(
            id = user.id,
            email = user.email,
            username = user.username,
            profileImageUrl = user.profileImageUrl,
            familyRole = user.familyRole?.name,
            familyRoleSelected = user.familyRoleSelected,
            familyId = user.family?.id
        )
    }
}