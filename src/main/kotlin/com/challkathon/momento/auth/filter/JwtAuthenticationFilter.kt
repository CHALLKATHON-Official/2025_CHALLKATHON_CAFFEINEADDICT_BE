package com.challkathon.momento.auth.filter

import com.challkathon.momento.auth.enums.TokenType
import com.challkathon.momento.auth.security.UserPrincipal
import com.challkathon.momento.auth.service.JwtService
import com.challkathon.momento.domain.user.repository.UserRepository
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository
) : OncePerRequestFilter() {

    private val logger = KotlinLogging.logger {}

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)
            logger.debug { "JWT from request: ${if (jwt != null) "Present" else "Not found"}" }
            
            if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt!!)) {
                val tokenType = jwtService.getTokenType(jwt)
                logger.debug { "Token type: $tokenType" }
                
                if (tokenType == TokenType.ACCESS) {
                    val userId = jwtService.extractUserId(jwt)
                    logger.debug { "Extracted userId: $userId" }
                    
                    val user = userRepository.findById(userId).orElse(null)
                    logger.debug { "User found: ${user != null}, User active: ${user?.isActive}" }
                    
                    if (user != null && user.isActive) {
                        val userPrincipal = UserPrincipal.create(user)
                        val authentication = UsernamePasswordAuthenticationToken(
                            userPrincipal, null, userPrincipal.authorities
                        )
                        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = authentication
                        logger.debug { "Successfully set authentication for user: ${user.email}" }
                    } else {
                        logger.warn { "User not found or inactive for userId: $userId" }
                    }
                } else {
                    logger.debug { "Invalid token type for authentication: $tokenType" }
                }
            } else {
                logger.debug { "JWT validation failed or no JWT found" }
            }
        } catch (ex: Exception) {
            logger.error { "Could not set user authentication in security context: ${ex.message}" }
        }

        filterChain.doFilter(request, response)
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        
        return if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}