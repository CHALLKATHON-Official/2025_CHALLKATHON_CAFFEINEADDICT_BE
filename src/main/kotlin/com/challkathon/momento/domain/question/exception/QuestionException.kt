package com.challkathon.momento.domain.question.exception

import com.challkathon.momento.domain.question.exception.code.QuestionErrorStatus
import com.challkathon.momento.global.exception.BaseException

class QuestionException(
    code: QuestionErrorStatus
) : BaseException(code)