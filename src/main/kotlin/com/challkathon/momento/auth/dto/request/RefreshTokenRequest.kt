package com.challkathon.momento.auth.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "토큰 갱신 요청")
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh Token은 필수입니다")
    @Schema(description = "갱신할 Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String
)