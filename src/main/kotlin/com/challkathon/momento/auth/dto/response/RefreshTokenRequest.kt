package com.challkathon.momento.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "Refresh Token 요청")
data class RefreshTokenRequest(
    @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @field:NotBlank(message = "리프레시 토큰은 필수입니다")
    val refreshToken: String
)