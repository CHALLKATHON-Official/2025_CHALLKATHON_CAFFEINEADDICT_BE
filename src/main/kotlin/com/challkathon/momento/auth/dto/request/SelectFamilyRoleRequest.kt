package com.challkathon.momento.auth.dto.request

import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import jakarta.validation.constraints.NotNull

/**
 * 가족 역할 선택 요청 DTO
 */
data class SelectFamilyRoleRequest(
    @field:NotNull(message = "가족 역할을 선택해주세요.")
    val familyRole: FamilyRole
)