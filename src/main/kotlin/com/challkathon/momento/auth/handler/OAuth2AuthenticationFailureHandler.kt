package com.challkathon.momento.auth.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class OAuth2AuthenticationFailureHandler : SimpleUrlAuthenticationFailureHandler() {

    private val logger = KotlinLogging.logger {}

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000")
            .queryParam("error", "authentication_failed")
            .queryParam("message", exception.localizedMessage)
            .build().toUriString()
        
        logger.error { "OAuth2 authentication failed: ${exception.message}" }
        
        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}