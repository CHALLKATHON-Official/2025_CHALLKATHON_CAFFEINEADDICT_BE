package com.challkathon.momento.auth.provider

import com.challkathon.momento.auth.enums.TokenType
import com.challkathon.momento.auth.exception.JwtAuthenticationException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtProvider {

    private val logger = LoggerFactory.getLogger(JwtProvider::class.java)

    @Value("\${jwt.secret}")
    private lateinit var secret: String

    @Value("\${jwt.access-token-expiration:3600000}") // 1시간
    private var accessTokenExpiration: Long = 3600000

    @Value("\${jwt.refresh-token-expiration:604800000}") // 7일
    private var refreshTokenExpiration: Long = 604800000

    @Value("\${jwt.issuer:momento}")
    private lateinit var issuer: String

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    // ========== 토큰 생성 메서드들 ==========

    /**
     * Access Token 생성
     */
    fun generateAccessToken(userDetails: UserDetails): String {
        return generateToken(
            subject = userDetails.username,
            expiration = accessTokenExpiration,
            tokenType = TokenType.ACCESS,
            additionalClaims = mapOf(
                "authorities" to userDetails.authorities.map { it.authority }
            )
        )
    }

    /**
     * Refresh Token 생성
     */
    fun generateRefreshToken(username: String): String {
        return generateToken(
            subject = username,
            expiration = refreshTokenExpiration,
            tokenType = TokenType.REFRESH
        )
    }
    
    /**
     * Refresh Token 생성 (버전 포함)
     */
    fun generateRefreshToken(username: String, version: Long): String {
        return generateToken(
            subject = username,
            expiration = refreshTokenExpiration,
            tokenType = TokenType.REFRESH,
            additionalClaims = mapOf("version" to version)
        )
    }

    /**
     * 토큰 생성 (내부 메서드)
     */
    private fun generateToken(
        subject: String,
        expiration: Long,
        tokenType: TokenType,
        additionalClaims: Map<String, Any> = emptyMap()
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(subject)
            .issuer(issuer)
            .issuedAt(now)
            .expiration(expiryDate)
            .claim("type", tokenType.name)
            .claims(additionalClaims)
            .signWith(key) // 0.12.x에서는 알고리즘 자동 감지
            .compact()
    }

    // ========== 토큰 추출 메서드들 ==========

    /**
     * 토큰에서 사용자명 추출
     */
    fun extractUsername(token: String): String {
        return extractClaim(token, Claims::getSubject)
    }

    /**
     * 토큰에서 사용자 ID 추출 (추가 기능)
     */
    fun extractUserId(token: String): Long? {
        return try {
            val userIdClaim = extractClaim(token) { claims: Claims ->
                claims["userId"]
            }
            userIdClaim?.toString()?.toLong()
        } catch (e: Exception) {
            logger.warn("토큰에서 사용자 ID를 추출할 수 없습니다", e)
            null
        }
    }

    /**
     * 토큰에서 권한 정보 추출
     */
    fun extractAuthorities(token: String): List<String> {
        return try {
            extractClaim(token) { claims: Claims ->
                @Suppress("UNCHECKED_CAST")
                claims["authorities"] as? List<String> ?: emptyList()
            }
        } catch (e: Exception) {
            logger.warn("토큰에서 권한 정보를 추출할 수 없습니다", e)
            emptyList()
        }
    }

    /**
     * 토큰에서 만료 시간 추출
     */
    fun extractExpiration(token: String): Date {
        return extractClaim(token, Claims::getExpiration)
    }

    /**
     * 토큰에서 발행 시간 추출
     */
    fun extractIssuedAt(token: String): Date {
        return extractClaim(token, Claims::getIssuedAt)
    }

    /**
     * 토큰 타입 추출
     */
    fun extractTokenType(token: String): TokenType? {
        return try {
            val type = extractClaim(token) { claims: Claims ->
                claims["type"] as? String
            }
            type?.let { TokenType.valueOf(it) }
        } catch (e: Exception) {
            logger.warn("토큰 타입을 추출할 수 없습니다", e)
            null
        }
    }
    
    /**
     * Refresh Token에서 버전 추출
     */
    fun extractRefreshTokenVersion(token: String): Long? {
        return try {
            extractClaim(token) { claims: Claims ->
                claims["version"]?.toString()?.toLong()
            }
        } catch (e: Exception) {
            logger.warn("Refresh Token 버전을 추출할 수 없습니다", e)
            null
        }
    }

    /**
     * 토큰에서 특정 클레임 추출
     */
    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    /**
     * 모든 클레임 추출
     */
    private fun extractAllClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (ex: io.jsonwebtoken.ExpiredJwtException) {
            logger.error("토큰 만료: ${ex.message}")
            throw ex // JwtAuthenticationFilter에서 처리
        } catch (ex: io.jsonwebtoken.MalformedJwtException) {
            logger.error("잘못된 토큰 형식: ${ex.message}")
            throw ex
        } catch (ex: io.jsonwebtoken.security.SignatureException) {
            logger.error("토큰 서명 검증 실패: ${ex.message}")
            throw ex
        } catch (ex: Exception) {
            logger.error("토큰 파싱 실패: ${ex.message}")
            throw JwtAuthenticationException(AuthErrorStatus._TOKEN_INVALID)
        }
    }

    // ========== 토큰 검증 메서드들 ==========

    /**
     * 토큰 유효성 검사 (UserDetails와 함께)
     */
    fun validateToken(token: String, userDetails: UserDetails): Boolean {
        return try {
            val username = extractUsername(token)
            val tokenType = extractTokenType(token)

            username == userDetails.username &&
                    !isTokenExpired(token) &&
                    tokenType == TokenType.ACCESS
        } catch (e: Exception) {
            logger.warn("토큰 검증 실패: ${e.message}")
            false
        }
    }

    /**
     * 토큰 유효성 검사 (사용자명만)
     */
    fun validateToken(token: String, username: String): Boolean {
        return try {
            val tokenUsername = extractUsername(token)
            tokenUsername == username && !isTokenExpired(token)
        } catch (e: Exception) {
            logger.warn("토큰 검증 실패: ${e.message}")
            false
        }
    }

    /**
     * Access Token 검증
     */
    fun validateAccessToken(token: String): Boolean {
        return try {
            val tokenType = extractTokenType(token)
            !isTokenExpired(token) && tokenType == TokenType.ACCESS
        } catch (e: Exception) {
            logger.warn("Access Token 검증 실패: ${e.message}")
            false
        }
    }

    /**
     * Refresh Token 검증
     */
    fun validateRefreshToken(token: String): Boolean {
        return try {
            val tokenType = extractTokenType(token)
            !isTokenExpired(token) && tokenType == TokenType.REFRESH
        } catch (e: Exception) {
            logger.warn("Refresh Token 검증 실패: ${e.message}")
            false
        }
    }
    
    /**
     * Refresh Token 검증 (버전 확인 포함)
     */
    fun validateRefreshToken(token: String, expectedVersion: Long): Boolean {
        return try {
            val tokenType = extractTokenType(token)
            val tokenVersion = extractRefreshTokenVersion(token)
            
            !isTokenExpired(token) && 
            tokenType == TokenType.REFRESH && 
            tokenVersion == expectedVersion
        } catch (e: Exception) {
            logger.warn("Refresh Token 검증 실패: ${e.message}")
            false
        }
    }

    /**
     * 토큰 만료 확인
     */
    private fun isTokenExpired(token: String): Boolean {
        return try {
            extractExpiration(token).before(Date())
        } catch (e: Exception) {
            logger.warn("토큰 만료 시간 확인 실패: ${e.message}")
            true // 에러 시 만료된 것으로 처리
        }
    }

    // ========== 유틸리티 메서드들 ==========

    /**
     * 토큰의 남은 유효시간 (밀리초)
     */
    fun getTokenRemainingTime(token: String): Long {
        return try {
            val expiration = extractExpiration(token)
            val now = Date()
            maxOf(0, expiration.time - now.time)
        } catch (e: Exception) {
            logger.warn("토큰 남은 시간 계산 실패: ${e.message}")
            0
        }
    }

    /**
     * Bearer 토큰에서 실제 토큰 추출
     */
    fun extractTokenFromBearer(bearerToken: String?): String? {
        return if (bearerToken?.startsWith("Bearer ") == true) {
            bearerToken.substring(7)
        } else null
    }

    /**
     * 토큰 정보 요약 (디버깅용)
     */
    fun getTokenInfo(token: String): TokenInfo? {
        return try {
            TokenInfo(
                username = extractUsername(token),
                tokenType = extractTokenType(token),
                issuedAt = extractIssuedAt(token),
                expiration = extractExpiration(token),
                isExpired = isTokenExpired(token),
                remainingTime = getTokenRemainingTime(token)
            )
        } catch (e: Exception) {
            logger.warn("토큰 정보 추출 실패: ${e.message}")
            null
        }
    }

    // ========== Refresh Token Rotation 관련 메서드들 ==========

    /**
     * 새로운 Access Token과 Refresh Token을 동시에 생성 (Rotation)
     * 기존 refresh token을 무효화하고 새로운 토큰 쌍을 반환
     */
    fun rotateTokens(userDetails: UserDetails, newVersion: Long): TokenPair {
        val newAccessToken = generateAccessToken(userDetails)
        val newRefreshToken = generateRefreshToken(userDetails.username, newVersion)
        
        logger.debug("토큰 rotation 완료: 사용자 = ${userDetails.username}, 새 버전 = $newVersion")
        
        return TokenPair(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken
        )
    }

    /**
     * Refresh Token 재사용 감지
     * 동일한 토큰으로 여러 번 갱신을 시도하는 경우 감지
     */
    fun detectTokenReuse(token: String, currentVersion: Long): Boolean {
        return try {
            val tokenVersion = extractRefreshTokenVersion(token)
            val isReused = tokenVersion != null && tokenVersion < currentVersion
            
            if (isReused) {
                logger.warn("Refresh Token 재사용 감지: 토큰 버전 = $tokenVersion, 현재 버전 = $currentVersion")
            }
            
            isReused
        } catch (e: Exception) {
            logger.warn("토큰 재사용 감지 실패: ${e.message}")
            true // 에러 시 재사용으로 간주하여 보안 강화
        }
    }

    /**
     * Access Token이 곧 만료되는지 확인 (자동 갱신 트리거용)
     * @param thresholdMinutes 만료 임계시간 (분 단위, 기본 5분)
     */
    fun isAccessTokenNearExpiry(token: String, thresholdMinutes: Long = 5): Boolean {
        return try {
            val remainingTime = getTokenRemainingTime(token)
            val thresholdMs = thresholdMinutes * 60 * 1000
            remainingTime <= thresholdMs
        } catch (e: Exception) {
            logger.warn("토큰 만료 임박 확인 실패: ${e.message}")
            true // 에러 시 만료 임박으로 간주
        }
    }

    /**
     * 토큰 쌍 유효성 검증
     * Access Token과 Refresh Token이 동일한 사용자에 대한 것인지 확인
     */
    fun validateTokenPair(accessToken: String, refreshToken: String): Boolean {
        return try {
            val accessUsername = extractUsername(accessToken)
            val refreshUsername = extractUsername(refreshToken)
            val accessType = extractTokenType(accessToken)
            val refreshType = extractTokenType(refreshToken)
            
            accessUsername == refreshUsername && 
            accessType == TokenType.ACCESS && 
            refreshType == TokenType.REFRESH &&
            !isTokenExpired(refreshToken)
        } catch (e: Exception) {
            logger.warn("토큰 쌍 검증 실패: ${e.message}")
            false
        }
    }
}

/**
 * 토큰 정보 데이터 클래스
 */
data class TokenInfo(
    val username: String,
    val tokenType: TokenType?,
    val issuedAt: Date,
    val expiration: Date,
    val isExpired: Boolean,
    val remainingTime: Long
)

/**
 * 토큰 쌍 데이터 클래스 (Rotation용)
 */
data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)