package com.challkathon.momento.domain.message.exception

import com.challkathon.momento.global.exception.BaseException
import com.challkathon.momento.global.exception.code.BaseCodeInterface

class MessageException(
    errorCode: BaseCodeInterface
) : BaseException(errorCode)
