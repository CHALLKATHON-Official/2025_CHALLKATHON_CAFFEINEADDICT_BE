package com.challkathon.momento.domain.family.exception.code


import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class FamilyErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {
    FAMILY_DID_NOT_SET(HttpStatus.BAD_REQUEST, "FAMILY4001", "가족 역할이 설정되지 않았습니다");

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