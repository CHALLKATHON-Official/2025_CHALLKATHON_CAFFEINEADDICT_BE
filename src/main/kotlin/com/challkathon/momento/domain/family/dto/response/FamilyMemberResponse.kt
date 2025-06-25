package com.challkathon.momento.domain.family.dto.response

import com.challkathon.momento.domain.user.entity.enums.FamilyRole
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "가족 구성원 응답 DTO")
data class FamilyMemberResponse(
    @Schema(description = "사용자 ID", example = "1")
    val userId: Long,
    
    @Schema(description = "사용자 이름", example = "홍길동")
    val name: String,
    
    @Schema(description = "가족 내 역할", example = "DAD")
    val familyRole: FamilyRole?,
    
    @Schema(description = "프로필 이미지 URL (없으면 null)", example = "https://example.com/profile.jpg", nullable = true)
    val profileImageUrl: String?
)