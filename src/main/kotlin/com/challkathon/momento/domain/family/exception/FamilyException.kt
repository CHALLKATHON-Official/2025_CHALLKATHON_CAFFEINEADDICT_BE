package com.challkathon.momento.domain.family.exception

import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.global.exception.BaseException

// 공통 상위 예외
open class FamilyException(
    code: FamilyErrorStatus
) : BaseException(code)

