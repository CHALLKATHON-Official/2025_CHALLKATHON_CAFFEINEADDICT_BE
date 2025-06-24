package com.challkathon.momento.domain.s3.exception.code

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class S3ErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_5001", "S3 파일 업로드에 실패했습니다."),
    S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_5002", "S3 파일 삭제에 실패했습니다."),
    S3_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "S3_4040", "S3에 파일이 존재하지 않습니다.");

    private val isSuccess = false

    override fun getCode(): BaseCode {
        return BaseCode(httpStatus, isSuccess, code, message)
    }
}
