package com.challkathon.momento.auth.handler

import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.global.common.BaseResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class CustomAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val requestURI = request.requestURI
        log.warn { "인증되지 않은 요청: $requestURI - Exception: ${authException.message}" }
        
        val errorCode = AuthErrorStatus._AUTHENTICATION_FAILED.getCode()
        
        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        
        val errorResponse = BaseResponse.onFailure<Any>(
            errorCode.code,
            "인증이 필요한 서비스입니다. 로그인 후 이용해주세요.",
            null
        )
        
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}