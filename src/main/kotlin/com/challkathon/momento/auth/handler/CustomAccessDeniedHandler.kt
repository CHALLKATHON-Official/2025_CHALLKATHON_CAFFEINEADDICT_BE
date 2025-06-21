package com.challkathon.momento.auth.handler

import com.challkathon.momento.auth.exception.code.AuthErrorStatus
import com.challkathon.momento.global.common.BaseResponse
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class CustomAccessDeniedHandler(
    private val objectMapper: ObjectMapper
) : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        log.warn { "접근 권한 없음: ${request.requestURI} - ${accessDeniedException.message}" }
        
        val errorCode = AuthErrorStatus._ACCESS_DENIED.getCode()
        
        response.status = errorCode.httpStatus.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        
        val errorResponse = BaseResponse.onFailure<Any>(
            errorCode.code,
            "해당 리소스에 접근할 권한이 없습니다.",
            null
        )
        
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}