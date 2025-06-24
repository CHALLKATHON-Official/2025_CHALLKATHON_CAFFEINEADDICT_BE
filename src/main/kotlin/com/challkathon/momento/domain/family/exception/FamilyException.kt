package com.challkathon.momento.domain.family.exception

import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.global.exception.BaseException

class FamilyException(
    code: FamilyErrorStatus
) : BaseException(code)