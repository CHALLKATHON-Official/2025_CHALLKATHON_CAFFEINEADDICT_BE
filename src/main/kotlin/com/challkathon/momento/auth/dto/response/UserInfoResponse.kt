package com.challkathon.momento.auth.dto.response

import com.challkathon.momento.domain.user.entity.enums.AuthProvider
import java.time.LocalDateTime

data class UserInfoResponse(
    val success: Boolean,
    val user: UserDetails
)

data class UserDetails(
    val userId: Long,
    val email: String,
    val username: String,
    val hasFamily: Boolean,
    val needsFamilyRole: Boolean,
    val familyId: Long?,
    val authProvider: AuthProvider,
    val profileImageUrl: String?,
    val lastLoginAt: LocalDateTime?
)