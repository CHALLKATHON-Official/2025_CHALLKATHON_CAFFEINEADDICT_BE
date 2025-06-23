package com.challkathon.momento.auth.dto.response

data class OAuthSuccessResponse(
    val success: Boolean,
    val userInfo: UserInfo
)

data class UserInfo(
    val userId: Long,
    val email: String,
    val username: String,
    val hasFamily: Boolean,
    val needsFamilyRole: Boolean,
    val familyId: Long?
)