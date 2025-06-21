package com.challkathon.momento.auth.handler

import com.challkathon.momento.auth.util.TokenCookieUtil
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2AuthenticationSuccessHandler(
    private val tokenCookieUtil: TokenCookieUtil
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        // OAuth2 성공 후 처리 로직
        setDefaultTargetUrl("/oauth2/redirect")
        super.onAuthenticationSuccess(request, response, authentication)
    }
}