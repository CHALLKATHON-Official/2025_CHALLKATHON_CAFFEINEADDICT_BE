package com.challkathon.momento.domain.family.exception

import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import com.challkathon.momento.global.exception.BaseException

// 공통 상위 예외
open class FamilyException(
    code: FamilyErrorStatus,
    override val message: String = code.getCode().message
) : BaseException(code)

class FamilyAlreadyJoinedException(
    override val message: String = "이미 가족에 소속되어 있습니다."
) : FamilyException(FamilyErrorStatus.FAMILY_ALREADY_JOINED, message)

class FamilyInviteCodeNotFoundException(
    override val message: String = "존재하지 않는 초대 코드입니다."
) : FamilyException(FamilyErrorStatus.INVITE_CODE_NOT_FOUND, message)

class FamilyNotJoinedException(
    override val message: String = "가족에 소속되어 있지 않습니다."
) : FamilyException(FamilyErrorStatus.FAMILY_NOT_JOINED, message)
