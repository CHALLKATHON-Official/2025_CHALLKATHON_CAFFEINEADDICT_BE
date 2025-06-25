package com.challkathon.momento.domain.s3.exception

import com.challkathon.momento.domain.s3.exception.code.S3ErrorStatus
import com.challkathon.momento.global.common.BaseResponse
import com.challkathon.momento.global.exception.BaseException
import com.challkathon.momento.global.exception.code.BaseCode
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

private val log = KotlinLogging.logger {}

@RestControllerAdvice
class S3ExceptionHandler {

    @ExceptionHandler(S3UploadException::class)
    fun handleUploadException(e: S3UploadException): ResponseEntity<BaseResponse<String>> {
        log.error(e) { "[handleUploadException] S3 업로드 실패: ${e.message}" }
        return handleExceptionInternal(e.getErrorCode())
    }

    @ExceptionHandler(S3DeleteException::class)
    fun handleDeleteException(e: S3DeleteException): ResponseEntity<BaseResponse<String>> {
        log.error(e) { "[handleDeleteException] S3 삭제 실패: ${e.message}" }
        return handleExceptionInternal(e.getErrorCode())
    }

    @ExceptionHandler(S3FileNotFoundException::class)
    fun handleFileNotFoundException(e: S3FileNotFoundException): ResponseEntity<BaseResponse<String>> {
        log.error(e) { "[handleFileNotFoundException] S3 파일 없음: ${e.message}" }
        return handleExceptionInternal(e.getErrorCode())
    }

    private fun handleExceptionInternal(errorCode: BaseCode): ResponseEntity<BaseResponse<String>> {
        return ResponseEntity
            .status(errorCode.httpStatus)
            .body(BaseResponse.onFailure(errorCode.code, errorCode.message, null))
    }
}
