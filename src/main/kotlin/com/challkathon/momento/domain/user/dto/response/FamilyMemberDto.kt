package com.challkathon.momento.domain.user.dto.response

data class FamilyMemberDto(
    val name: String,                    // 구성원 이름
    val role: String,                    // 역할 (아빠, 엄마, 자녀 등)
    val profileImageUrl: String?         // 프로필 이미지 URL
)
