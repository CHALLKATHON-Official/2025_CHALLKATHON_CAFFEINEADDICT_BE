package com.challkathon.momento.auth.dto.response

data class UserInfo(
    val id: Long,
    val email: String,
    val username: String,
    val profileImageUrl: String?,
    val familyRole: String?,
    val familyRoleSelected: Boolean,
    val familyId: Long?
)