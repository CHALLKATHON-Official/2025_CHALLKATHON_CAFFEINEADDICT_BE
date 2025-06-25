package com.challkathon.momento.domain.user.dto.response

data class MyPageResponse(
    val name: String,                    // 내 이름
    val role: String,                    // 내 가족 내 역할 (예: 아빠, 자녀1 등)
    val profileImageUrl: String?,        // 내 프로필 이미지

    val inviteCode: String,              // 우리 가족 초대코드

    val familyMembers: List<FamilyMemberDto> // 가족 구성원 리스트
)
