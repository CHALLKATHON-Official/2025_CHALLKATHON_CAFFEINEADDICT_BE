package com.challkathon.momento.domain.question.exception.code

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class QuestionErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION4001", "질문을 찾을 수 없습니다"),
    QUESTION_INVALID_ACCESS(HttpStatus.FORBIDDEN, "QUESTION4002", "해당 질문에 접근할 권한이 없습니다"),
    QUESTION_ALREADY_ANSWERED(HttpStatus.BAD_REQUEST, "QUESTION4003", "이미 답변이 있는 질문은 재생성할 수 없습니다"),
    QUESTION_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "QUESTION4004", "새 질문을 생성할 수 없습니다"),
    FAMILY_QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION4005", "가족 질문을 찾을 수 없습니다");

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