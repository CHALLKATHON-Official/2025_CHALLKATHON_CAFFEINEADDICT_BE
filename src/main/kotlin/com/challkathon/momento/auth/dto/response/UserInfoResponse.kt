package com.challkathon.momento.auth.dto.response

import com.challkathon.momento.domain.user.entity.enums.AuthProvider
import com.challkathon.momento.domain.user.entity.enums.Role

data class UserInfoResponse(
    val id: Long,
    val email: String,
    val username: String,
    val role: Role,
    val provider: AuthProvider
)