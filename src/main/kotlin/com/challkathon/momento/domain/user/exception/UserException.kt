package com.challkathon.momento.domain.user.exception

import com.challkathon.momento.domain.user.exception.code.UserErrorStatus
import com.challkathon.momento.global.exception.BaseException

class UserException(
    code: UserErrorStatus
) : BaseException(code)