package com.challkathon.momento.auth.service

import com.challkathon.momento.auth.enums.TokenType
import com.challkathon.momento.auth.exception.AuthException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.Keys
import io.jsonwebtoken.security.SignatureException
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.crypto.SecretKey
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class JwtService(
    @Value("\${jwt.secret}")
    private val jwtSecret: String,
    
    @Value("\${jwt.issuer}")
    private val issuer: String,
    
    @Value("\${jwt.access-token-expiration}")
    private val accessTokenExpiration: Long,
    
    @Value("\${jwt.refresh-token-expiration}")
    private val refreshTokenExpiration: Long
) {
    private val logger = KotlinLogging.logger {}

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtSecret.toByteArray())
    }

    fun generateAccessToken(userId: Long, email: String): String {
        val now = Date()
        val expiration = Date(now.time + accessTokenExpiration)

        return Jwts.builder()
            .issuer(issuer)
            .subject(userId.toString())
            .claim("email", email)
            .claim("type", TokenType.ACCESS.name)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    fun generateRefreshToken(userId: Long): String {
        val now = Date()
        val expiration = Date(now.time + refreshTokenExpiration)

        return Jwts.builder()
            .issuer(issuer)
            .subject(userId.toString())
            .claim("type", TokenType.REFRESH.name)
            .issuedAt(now)
            .expiration(expiration)
            .signWith(key)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: SignatureException) {
            logger.warn { "Invalid JWT signature: ${e.message}" }
            false
        } catch (e: MalformedJwtException) {
            logger.warn { "Invalid JWT token: ${e.message}" }
            false
        } catch (e: ExpiredJwtException) {
            logger.warn { "Expired JWT token: ${e.message}" }
            false
        } catch (e: UnsupportedJwtException) {
            logger.warn { "Unsupported JWT token: ${e.message}" }
            false
        } catch (e: IllegalArgumentException) {
            logger.warn { "JWT claims string is empty: ${e.message}" }
            false
        } catch (e: Exception) {
            logger.warn { "Invalid JWT token: ${e.message}" }
            false
        }
    }

    fun extractUserId(token: String): Long {
        return try {
            val claims = getClaims(token)
            claims.subject.toLong()
        } catch (e: Exception) {
            logger.warn { "Failed to extract user ID from token: ${e.message}" }
            throw AuthException(AuthErrorStatus._TOKEN_INVALID)
        }
    }

    fun extractEmail(token: String): String {
        return try {
            val claims = getClaims(token)
            claims.get("email", String::class.java)
        } catch (e: Exception) {
            logger.warn { "Failed to extract email from token: ${e.message}" }
            throw AuthException(AuthErrorStatus._TOKEN_INVALID)
        }
    }

    fun getTokenType(token: String): TokenType {
        return try {
            val claims = getClaims(token)
            val type = claims.get("type", String::class.java)
            TokenType.valueOf(type)
        } catch (e: Exception) {
            logger.warn { "Failed to extract token type: ${e.message}" }
            throw AuthException(AuthErrorStatus._TOKEN_INVALID)
        }
    }

    fun getExpirationDate(token: String): LocalDateTime {
        return try {
            val claims = getClaims(token)
            claims.expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
        } catch (e: Exception) {
            logger.warn { "Failed to extract expiration date: ${e.message}" }
            throw AuthException(AuthErrorStatus._TOKEN_INVALID)
        }
    }

    private fun getClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: SignatureException) {
            throw AuthException(AuthErrorStatus._TOKEN_SIGNATURE_INVALID)
        } catch (e: MalformedJwtException) {
            throw AuthException(AuthErrorStatus._TOKEN_MALFORMED)
        } catch (e: ExpiredJwtException) {
            throw AuthException(AuthErrorStatus._TOKEN_EXPIRED)
        } catch (e: UnsupportedJwtException) {
            throw AuthException(AuthErrorStatus._TOKEN_UNSUPPORTED)
        } catch (e: IllegalArgumentException) {
            throw AuthException(AuthErrorStatus._TOKEN_INVALID)
        } catch (e: Exception) {
            throw AuthException(AuthErrorStatus._JWT_AUTHENTICATION_FAILED)
        }
    }
}