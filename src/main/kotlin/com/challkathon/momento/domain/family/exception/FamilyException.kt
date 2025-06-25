package com.challkathon.momento.domain.family.exception

import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.global.exception.BaseException

// 공통 상위 예외
open class FamilyException(
    errorStatus: FamilyErrorStatus
) : BaseException(errorStatus)

class FamilyAlreadyJoinedException(
    message: String = "이미 가족에 소속되어 있습니다."
) : FamilyException(FamilyErrorStatus.FAMILY_ALREADY_JOINED)

class FamilyInviteCodeNotFoundException(
    message: String = "존재하지 않는 초대 코드입니다."
) : FamilyException(FamilyErrorStatus.INVITE_CODE_NOT_FOUND)

class FamilyNotJoinedException(
    message: String = "가족에 소속되어 있지 않습니다."
) : FamilyException(FamilyErrorStatus.FAMILY_NOT_JOINED)
