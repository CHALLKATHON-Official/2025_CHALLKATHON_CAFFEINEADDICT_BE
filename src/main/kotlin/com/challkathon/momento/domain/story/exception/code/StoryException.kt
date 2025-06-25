package com.challkathon.momento.domain.story.exception

import com.challkathon.momento.domain.story.exception.code.StoryErrorStatus
import com.challkathon.momento.global.exception.BaseException

// 공통 상위 예외
open class StoryException(
    code: StoryErrorStatus,
    override val message: String = code.getCode().message
) : BaseException(code)

class StoryNotFoundException(
    override val message: String = "스토리를 찾을 수 없습니다."
) : StoryException(StoryErrorStatus.STORY_NOT_FOUND, message)
