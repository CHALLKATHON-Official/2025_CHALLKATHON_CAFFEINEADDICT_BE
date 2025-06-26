package com.challkathon.momento.domain.story.exception

import com.challkathon.momento.global.exception.BaseException
import com.challkathon.momento.global.exception.code.BaseCodeInterface

class StoryException(
    errorCode: BaseCodeInterface,
) : BaseException(errorCode)
