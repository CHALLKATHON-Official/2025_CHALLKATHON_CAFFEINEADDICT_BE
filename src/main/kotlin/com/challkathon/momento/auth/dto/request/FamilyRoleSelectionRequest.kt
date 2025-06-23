package com.challkathon.momento.auth.dto.request

import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "가족 역할 선택 요청")
data class FamilyRoleSelectionRequest(
    @field:NotNull(message = "가족 역할을 선택해주세요")
    @Schema(description = "가족 역할", example = "DAD", allowableValues = ["DAD", "MOM", "SON", "DAUGHTER"])
    val familyRole: FamilyRole
)