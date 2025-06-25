package com.challkathon.momento.auth.exception

import com.challkathon.momento.global.exception.BaseException
import com.challkathon.momento.global.exception.code.BaseCodeInterface

class AuthException(
    errorCode: BaseCodeInterface
) : BaseException(errorCode)