package com.challkathon.momento.auth.dto.response

import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "가족 역할 상태 응답")
data class FamilyRoleStatusResponse(
    @Schema(description = "가족 역할 선택 여부", example = "false")
    val familyRoleSelected: Boolean,
    
    @Schema(description = "선택된 가족 역할", example = "DAD", nullable = true)
    val familyRole: String?,
    
    @Schema(description = "선택 가능한 가족 역할 목록")
    val availableRoles: List<FamilyRoleOption>
)

@Schema(description = "가족 역할 옵션")
data class FamilyRoleOption(
    @Schema(description = "역할 코드", example = "DAD")
    val code: String,
    
    @Schema(description = "역할 설명", example = "아빠")
    val description: String
)