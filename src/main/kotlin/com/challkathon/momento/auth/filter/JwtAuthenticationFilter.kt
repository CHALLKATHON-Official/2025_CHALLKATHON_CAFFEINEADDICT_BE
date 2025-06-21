package com.challkathon.momento.auth.filter

import com.challkathon.momento.auth.exception.JwtAuthenticationException
import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.auth.provider.JwtProvider
import com.challkathon.momento.auth.service.CustomUserDetailsService
import com.challkathon.momento.global.common.BaseResponse
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter

private val log = KotlinLogging.logger {}

@Component
class JwtAuthenticationFilter(
    private val jwtProvider: JwtProvider,
    private val userDetailsService: CustomUserDetailsService,
    private val objectMapper: ObjectMapper
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    // 인증을 건너뛸 경로들
    private val excludedPaths = listOf(
        "/api/v1/auth/signup",
        "/api/v1/auth/signin",
        "/api/v1/auth/refresh",
        "/oauth2/**",
        "/login/**",
        "/login/oauth2/**",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/v3/api-docs",
        "/swagger-resources/**",
        "/webjars/**",
        "/h2-console/**",
        "/api/v1/test/public",
        "/favicon.ico",
        "/error",
        "/",
        "/profile",
        "/oauth2/redirect" // OAuth2 리다이렉트 페이지는 인증 없이 접근 가능
    )

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        val contextPath = request.contextPath
        val pathToCheck = if (contextPath.isNotEmpty() && path.startsWith(contextPath)) {
            path.substring(contextPath.length)
        } else {
            path
        }

        val shouldExclude = excludedPaths.any { pattern ->
            pathMatcher.match(pattern, pathToCheck)
        }

        if (shouldExclude) {
            log.debug { "JWT 필터 제외 경로: $pathToCheck" }
        }

        return shouldExclude
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val jwt = getJwtFromRequest(request)

            // JWT가 있을 경우에만 처리
            if (!jwt.isNullOrBlank()) {
                // 이미 인증되어 있지 않은 경우에만 처리
                if (SecurityContextHolder.getContext().authentication == null) {
                    processJwtAuthentication(jwt, request)
                }
            }
            // JWT가 없으면 그냥 다음 필터로 통과
        } catch (ex: ExpiredJwtException) {
            log.debug { "토큰이 만료되었습니다: ${ex.message}" }
            setErrorResponse(response, AuthErrorStatus._TOKEN_EXPIRED)
            return
        } catch (ex: MalformedJwtException) {
            log.debug { "잘못된 토큰 형식입니다: ${ex.message}" }
            setErrorResponse(response, AuthErrorStatus._TOKEN_MALFORMED)
            return
        } catch (ex: SignatureException) {
            log.debug { "토큰 서명이 유효하지 않습니다: ${ex.message}" }
            setErrorResponse(response, AuthErrorStatus._TOKEN_SIGNATURE_INVALID)
            return
        } catch (ex: UnsupportedJwtException) {
            log.debug { "지원하지 않는 토큰입니다: ${ex.message}" }
            setErrorResponse(response, AuthErrorStatus._TOKEN_UNSUPPORTED)
            return
        } catch (ex: JwtAuthenticationException) {
            log.debug { "JWT 인증 실패: ${ex.message}" }
            setErrorResponse(response, AuthErrorStatus._JWT_AUTHENTICATION_FAILED)
            return
        } catch (ex: UsernameNotFoundException) {
            log.debug { "사용자를 찾을 수 없습니다: ${ex.message}" }
            setErrorResponse(response, AuthErrorStatus._USER_NOT_FOUND)
            return
        } catch (ex: Exception) {
            log.error(ex) { "예상치 못한 인증 오류" }
            setErrorResponse(response, AuthErrorStatus._AUTHENTICATION_FAILED)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun processJwtAuthentication(jwt: String, request: HttpServletRequest) {
        // 토큰 유효성 검사 (Access Token인지 확인)
        if (!jwtProvider.validateAccessToken(jwt)) {
            throw JwtAuthenticationException(AuthErrorStatus._TOKEN_INVALID)
        }

        // 토큰에서 사용자명 추출
        val username = jwtProvider.extractUsername(jwt)
        log.debug { "JWT에서 추출한 사용자: $username" }

        // 사용자 정보 로드
        val userDetails = userDetailsService.loadUserByUsername(username)

        // Authentication 객체 생성 및 SecurityContext에 설정
        val authentication = UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.authorities
        )
        authentication.details = WebAuthenticationDetailsSource().buildDetails(request)

        SecurityContextHolder.getContext().authentication = authentication
        log.debug { "사용자 인증 성공: $username" }
    }

    private fun getJwtFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken?.startsWith("Bearer ") == true) {
            bearerToken.substring(7)
        } else {
            null
        }
    }

    private fun setErrorResponse(
        response: HttpServletResponse,
        errorStatus: AuthErrorStatus
    ) {
        val errorCode = errorStatus.getCode()
        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = BaseResponse.onFailure<Any>(
            errorCode.code,
            errorCode.message,
            null
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}