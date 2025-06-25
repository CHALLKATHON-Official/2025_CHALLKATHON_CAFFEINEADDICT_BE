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
    private val cookieHttpOnly: Boolean
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
    }
}