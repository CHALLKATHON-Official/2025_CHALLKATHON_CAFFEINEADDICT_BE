package com.challkathon.momento.auth.dto.response

import com.challkathon.momento.auth.dto.response.UserInfoResponse

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userInfo: UserInfoResponse
)