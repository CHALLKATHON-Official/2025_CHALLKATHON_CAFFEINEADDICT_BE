package com.challkathon.momento.domain.family.exception.code

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class FamilyErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {

    FAMILY_DID_NOT_SET(HttpStatus.BAD_REQUEST, "FAMILY_400_1", "가족 역할이 설정되지 않았습니다"),
    FAMILY_ALREADY_JOINED(HttpStatus.CONFLICT, "FAMILY_409_1", "이미 가족에 소속되어 있습니다."),
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "FAMILY_404_1", "존재하지 않는 초대 코드입니다."),
    FAMILY_NOT_JOINED(HttpStatus.BAD_REQUEST, "FAMILY_400_2", "가족에 소속되어 있지 않습니다."),
    FAMILY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "FAMILY_403_1", "해당 가족에 접근할 권한이 없습니다.");

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

