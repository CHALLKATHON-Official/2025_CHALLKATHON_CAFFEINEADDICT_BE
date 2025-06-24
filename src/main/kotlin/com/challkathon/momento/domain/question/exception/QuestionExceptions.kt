package com.challkathon.momento.domain.question.exception

import com.challkathon.momento.global.exception.BaseException
import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class QuestionErrorStatus(
    private val code: String,
    private val message: String,
    private val httpStatus: HttpStatus
) : BaseCodeInterface {
    AI_GENERATION_FAILED("Q001", "AI 질문 생성에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    QUESTION_NOT_FOUND("Q002", "질문을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_QUESTION_ACCESS("Q003", "해당 질문에 접근할 권한이 없습니다", HttpStatus.FORBIDDEN),
    QUESTION_ALREADY_ANSWERED("Q004", "이미 답변한 질문입니다", HttpStatus.CONFLICT),
    FAMILY_NOT_FOUND("Q005", "가족 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    ASSISTANT_INIT_FAILED("Q006", "AI Assistant 초기화에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    
    override fun getCode(): BaseCode {
        return BaseCode(
            httpStatus = this.httpStatus,
            isSuccess = false,
            code = this.code,
            message = this.message
        )
    }
}

class AIQuestionGenerationException(
    errorStatus: QuestionErrorStatus = QuestionErrorStatus.AI_GENERATION_FAILED
) : BaseException(errorStatus)

class QuestionNotFoundException(
    questionId: Long? = null
) : BaseException(QuestionErrorStatus.QUESTION_NOT_FOUND)

class InvalidQuestionAccessException : BaseException(QuestionErrorStatus.INVALID_QUESTION_ACCESS)
