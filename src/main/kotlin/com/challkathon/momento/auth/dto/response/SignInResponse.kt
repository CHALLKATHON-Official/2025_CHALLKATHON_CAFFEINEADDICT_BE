package com.challkathon.momento.auth.dto.response

import com.challkathon.momento.domain.user.entity.enums.AuthProvider
import com.challkathon.momento.domain.user.entity.enums.Role
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "로그인/회원가입 응답 DTO (토큰은 헤더로 전달)")
data class SignInResponse(
    @Schema(description = "사용자 정보")
    val user: UserInfoResponse
)