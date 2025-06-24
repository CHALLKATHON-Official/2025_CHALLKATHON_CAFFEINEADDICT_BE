package com.challkathon.momento.domain.user.exception.code

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class UserErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER4001", "사용자를 찾을 수 없습니다"),
    USER_FAMILY_NOT_SET(HttpStatus.BAD_REQUEST, "USER4002", "가족이 설정되지 않았습니다"),
    USER_INVALID_ACCESS(HttpStatus.FORBIDDEN, "USER4003", "해당 사용자에 접근할 권한이 없습니다"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER4004", "이미 존재하는 사용자입니다"),
    USER_INACTIVE(HttpStatus.FORBIDDEN, "USER4005", "비활성화된 사용자입니다"),
    USER_EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "USER4006", "이메일 인증이 완료되지 않았습니다");

    private val isSuccess = false

    override fun getCode(): BaseCode {
        return BaseCode(
            httpStatus = httpStatus,
            isSuccess = isSuccess,
            code = code,
            message = message
        )
    }
}