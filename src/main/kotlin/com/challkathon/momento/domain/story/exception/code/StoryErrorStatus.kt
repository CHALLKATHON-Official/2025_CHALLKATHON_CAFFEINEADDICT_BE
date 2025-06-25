package com.challkathon.momento.domain.story.exception.code

import com.challkathon.momento.global.exception.code.BaseCode
import com.challkathon.momento.global.exception.code.BaseCodeInterface
import org.springframework.http.HttpStatus

enum class StoryErrorStatus(
    private val httpStatus: HttpStatus,
    private val code: String,
    private val message: String
) : BaseCodeInterface {

    STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "STORY_404_1", "스토리를 찾을 수 없습니다.");

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
