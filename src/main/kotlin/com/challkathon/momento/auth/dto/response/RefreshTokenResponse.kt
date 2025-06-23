package com.challkathon.momento.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 갱신 응답")
data class RefreshTokenResponse(
    @Schema(description = "새로운 Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,
    
    @Schema(description = "Access Token 만료시간 (초)", example = "3600")
    val expiresIn: Long = 3600,
    
    @Schema(description = "토큰 타입", example = "Bearer")
    val tokenType: String = "Bearer",
    
    @Schema(description = "갱신 성공 메시지", example = "토큰이 성공적으로 갱신되었습니다.")
    val message: String = "토큰이 성공적으로 갱신되었습니다."
)