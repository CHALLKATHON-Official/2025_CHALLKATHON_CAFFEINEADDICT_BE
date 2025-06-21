package com.challkathon.momento.global.exception

import com.challkathon.momento.global.common.BaseResponse
import com.challkathon.momento.global.exception.BaseException
import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.GlobalErrorStatus
import mu.KotlinLogging
import org.hibernate.exception.ConstraintViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.security.core.AuthenticationException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.nio.file.AccessDeniedException

private val log = KotlinLogging.logger {}

@RestControllerAdvice(annotations = [RestController::class])
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    /**
     * 1) 직접 정의한 비즈니스/도메인 예외 처리
     */
    @ExceptionHandler(BaseException::class)
    fun handleRestApiException(e: BaseException): ResponseEntity<BaseResponse<String>> {
        val errorCode = e.getErrorCode()
        log.error(e) { "[handleRestApiException] ${e.message}" }
        return handleExceptionInternal(errorCode)
    }

    /**
     * 2) 예상치 못한 모든 서버 예외 처리
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<BaseResponse<String>> {
        log.error(e) { "[handleException] ${e.message}" }

        // _INTERNAL_SERVER_ERROR 코드와 함께, 실제 발생한 메시지를 추가
        return handleExceptionInternalFalse(GlobalErrorStatus._INTERNAL_SERVER_ERROR.getCode(), e.message)
    }

    /**
     * 3) ConstraintViolationException (파라미터 검증, 메서드 검증 실패)
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(e: ConstraintViolationException): ResponseEntity<BaseResponse<String>> {
        log.error(e) { "[handleConstraintViolationException] ${e.message}" }
        return handleExceptionInternal(GlobalErrorStatus._VALIDATION_ERROR.getCode())
    }

    /**
     * 4) MethodArgumentTypeMismatchException
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<BaseResponse<String>> {
        log.error(e) { "[handleMethodArgumentTypeMismatch] ${e.message}" }
        return handleExceptionInternal(GlobalErrorStatus._METHOD_ARGUMENT_ERROR.getCode())
    }

    /**
     * 5) MethodArgumentNotValidException
     */
    override fun handleMethodArgumentNotValid(
        e: MethodArgumentNotValidException,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errors = mutableMapOf<String, String>()

        e.bindingResult.fieldErrors.forEach { fieldError ->
            val fieldName = fieldError.field
            val errorMessage = fieldError.defaultMessage ?: ""
            errors.merge(fieldName, errorMessage) { oldVal, newVal -> "$oldVal, $newVal" }
        }

        log.error(e) { "[handleMethodArgumentNotValid] ${e.message}" }
        return handleExceptionInternalArgs(GlobalErrorStatus._VALIDATION_ERROR.getCode(), errors)
    }

    /**
     * 6) Spring Security 인증/인가 예외 처리
     */
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<BaseResponse<String>> {
        log.error(e) { "[handleAuthenticationException] ${e.message}" }
        return handleExceptionInternal(GlobalErrorStatus._UNAUTHORIZED.getCode())
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(e: AccessDeniedException): ResponseEntity<BaseResponse<String>> {
        log.error(e) { "[handleAccessDeniedException] ${e.message}" }
        return handleExceptionInternal(GlobalErrorStatus._ACCESS_DENIED.getCode())
    }

    /**
     * 7) NoHandlerFoundException (404 Not Found)
     */
    override fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        log.error { "No handler found for ${ex.httpMethod} ${ex.requestURL}" }
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(BaseResponse.onFailure("COMMON404", "요청하신 URL을 찾을 수 없습니다.", null))
    }

    /**
     * 8) HttpRequestMethodNotSupportedException (405 Method Not Allowed)
     */
    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val supportedMethods = ex.supportedHttpMethods?.joinToString(", ") { it.toString() } ?: ""

        log.error { "Method not supported: ${ex.message}" }
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(
                BaseResponse.onFailure(
                    "COMMON405",
                    "지원하지 않는 HTTP 메서드입니다. 지원되는 메서드: $supportedMethods",
                    null
                )
            )
    }

    /**
     * ==============
     * 내부 메서드
     * ==============
     */
    private fun handleExceptionInternal(errorCode: BaseCode): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(BaseResponse.onFailure(errorCode.code, errorCode.message, null))
    }

    private fun handleExceptionInternalArgs(errorCode: BaseCode, errorArgs: Map<String, String>): ResponseEntity<Any> {
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(BaseResponse.onFailure(errorCode.code, errorCode.message, errorArgs))
    }

    private fun handleExceptionInternalFalse(
        errorCode: BaseCode,
        errorPoint: String?
    ): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(BaseResponse.onFailure(errorCode.code, errorCode.message, errorPoint))
    }
}