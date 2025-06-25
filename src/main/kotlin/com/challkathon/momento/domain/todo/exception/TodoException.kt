package com.challkathon.momento.domain.todo.exception

import com.challkathon.momento.global.exception.BaseException
import com.challkathon.momento.global.exception.code.BaseCodeInterface

class TodoException(
    errorStatus: BaseCodeInterface
) : BaseException(errorStatus)