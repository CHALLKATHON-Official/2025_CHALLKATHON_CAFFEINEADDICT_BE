package com.challkathon.momento.auth.util

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CookieUtil(
    @Value("\${app.cookie.refresh-token.name}")
    private val refreshTokenCookieName: String,
    
    @Value("\${app.cookie.refresh-token.max-age}")
    private val refreshTokenMaxAge: Int,
    
    @Value("\${app.cookie.refresh-token.domain}")
    private val cookieDomain: String,
    
    @Value("\${app.cookie.refresh-token.secure}")
    private val cookieSecure: Boolean,
    
    @Value("\${app.cookie.refresh-token.http-only}")
    private val cookieHttpOnly: Boolean,
    
    @Value("\${app.cookie.refresh-token.same-site}")
    private val cookieSameSite: String
) {

    fun addRefreshTokenCookie(response: HttpServletResponse, refreshToken: String) {
        val cookie = Cookie(refreshTokenCookieName, refreshToken).apply {
            maxAge = refreshTokenMaxAge
            domain = cookieDomain
            path = "/"
            isHttpOnly = cookieHttpOnly
            secure = cookieSecure
        }
        response.addCookie(cookie)
        
        // SameSite 설정을 위한 추가 헤더
        val sameSiteHeader = "${cookie.name}=${cookie.value}; " +
                "Max-Age=${cookie.maxAge}; " +
                "Domain=${cookie.domain}; " +
                "Path=${cookie.path}; " +
                (if (cookie.isHttpOnly) "HttpOnly; " else "") +
                (if (cookie.secure) "Secure; " else "") +
                "SameSite=$cookieSameSite"
        
        response.addHeader("Set-Cookie", sameSiteHeader)
    }

    fun getRefreshTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.find { it.name == refreshTokenCookieName }?.value
    }

    fun deleteRefreshTokenCookie(response: HttpServletResponse) {
        val cookie = Cookie(refreshTokenCookieName, "").apply {
            maxAge = 0
            domain = cookieDomain
            path = "/"
            isHttpOnly = cookieHttpOnly
            secure = cookieSecure
        }
        response.addCookie(cookie)
        
        // SameSite 설정을 위한 추가 헤더 (삭제용)
        val sameSiteHeader = "${cookie.name}=; " +
                "Max-Age=0; " +
                "Domain=${cookie.domain}; " +
                "Path=${cookie.path}; " +
                (if (cookie.isHttpOnly) "HttpOnly; " else "") +
                (if (cookie.secure) "Secure; " else "") +
                "SameSite=$cookieSameSite"
        
        response.addHeader("Set-Cookie", sameSiteHeader)
    }
}