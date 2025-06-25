package com.challkathon.momento.domain.story.exception

import com.challkathon.momento.domain.story.exception.code.StoryErrorStatus
import com.challkathon.momento.global.exception.BaseException

// 공통 상위 예외
open class StoryException(
    code: StoryErrorStatus,
) : BaseException(code)
