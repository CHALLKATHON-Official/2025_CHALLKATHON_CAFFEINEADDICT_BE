package com.challkathon.momento.auth.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component


@Component
class TokenCookieUtil {

    @Value("\${app.cookie.refresh-token.name:refreshToken}")
    private lateinit var refreshTokenCookieName: String

    @Value("\${app.cookie.refresh-token.max-age:604800}") // 7일 (초 단위)
    private var refreshTokenMaxAge: Int = 604800

    @Value("\${app.cookie.domain:localhost}")
    private lateinit var cookieDomain: String

    @Value("\${app.cookie.refresh-token.secure:true}")
    private var cookieSecure: Boolean = true

    companion object {
        const val ACCESS_TOKEN_HEADER = "Authorization"
        const val TOKEN_PREFIX = "Bearer "
    }

    /**
     * Access Token을 응답 헤더에 설정
     */
    fun setAccessTokenHeader(response: HttpServletResponse, accessToken: String) {
        response.setHeader(ACCESS_TOKEN_HEADER, "$TOKEN_PREFIX$accessToken")
    }


    /**
     * Refresh Token을 HttpOnly 쿠키에 설정
     */
    fun setRefreshTokenCookie(response: HttpServletResponse, refreshToken: String) {
        val cookie = Cookie(refreshTokenCookieName, refreshToken).apply {
            maxAge = refreshTokenMaxAge
            isHttpOnly = true
            secure = cookieSecure
            path = "/"
        }
        response.addCookie(cookie)

        // SameSite=None 설정을 위한 추가 헤더 (Jakarta Cookie가 지원하지 않음)
        response.addHeader(
            "Set-Cookie",
            "$refreshTokenCookieName=$refreshToken; Max-Age=$refreshTokenMaxAge; Path=/; HttpOnly; Secure; SameSite=None"
        )
    }

    /**
     * 요청에서 Refresh Token 쿠키 추출
     */
    fun getRefreshTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies
            ?.find { it.name == refreshTokenCookieName }
            ?.value
    }

    /**
     * 요청 헤더에서 Access Token 추출
     */
    fun getAccessTokenFromHeader(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader(ACCESS_TOKEN_HEADER)
        return if (bearerToken?.startsWith(TOKEN_PREFIX) == true) {
            bearerToken.substring(TOKEN_PREFIX.length)
        } else null
    }

    /**
     * Refresh Token 쿠키 삭제 (로그아웃 시 사용)
     */
    fun deleteRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = Cookie(refreshTokenCookieName, null).apply {
            maxAge = 0
            isHttpOnly = true
            secure = cookieSecure
            path = "/"
        }
        response.addCookie(cookie)

        // SameSite=None으로 쿠키 삭제
        response.addHeader(
            "Set-Cookie",
            "$refreshTokenCookieName=; Max-Age=0; Path=/; HttpOnly; Secure; SameSite=None"
        )
    }

    /**
     * 모든 토큰 정리 (로그아웃 시 사용)
     */
    fun clearAllTokens(response: HttpServletResponse) {
        // Authorization 헤더 초기화
        response.setHeader(ACCESS_TOKEN_HEADER, "")

        // Refresh Token 쿠키 삭제
        deleteRefreshTokenCookie(response)
    }

    /**
     * 토큰 설정 (회원가입, 로그인 시 사용)
     * Access Token은 Authorization 헤더에만,
     * Refresh Token은 httpOnly 쿠키에만 설정
     */
    fun setTokens(response: HttpServletResponse, accessToken: String, refreshToken: String) {
        // Access Token은 오직 Authorization 헤더로만
        setAccessTokenHeader(response, accessToken)
        // Refresh Token은 오직 httpOnly 쿠키로만
        setRefreshTokenCookie(response, refreshToken)
    }

    fun createCookie(key: String, value: String): ResponseCookie {
        return ResponseCookie.from(key, value)
            .httpOnly(false)
            .secure(true)
            .sameSite("None")
            .maxAge(60 * 60 * 60 * 10)
            .path("/")
            .build()
    }
}