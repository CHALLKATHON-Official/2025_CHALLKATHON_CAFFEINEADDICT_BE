package com.challkathon.momento.domain.message.exception.code

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class MessageErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {

    _MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "MESSAGE4001", "해당 날짜의 메시지를 찾을 수 없습니다.");

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
