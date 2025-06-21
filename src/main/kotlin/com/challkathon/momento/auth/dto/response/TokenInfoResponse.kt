package com.challkathon.momento.auth.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "토큰 정보 응답")
data class TokenInfoResponse(
    @Schema(description = "사용자명", example = "user@example.com")
    val username: String,
    
    @Schema(description = "토큰 타입", example = "ACCESS")
    val tokenType: String,
    
    @Schema(description = "토큰 발급 시간", example = "2024-01-01T00:00:00.000Z")
    val issuedAt: String,
    
    @Schema(description = "토큰 만료 시간", example = "2024-01-01T01:00:00.000Z")
    val expiration: String,
    
    @Schema(description = "토큰 만료 여부", example = "false")
    val isExpired: Boolean,
    
    @Schema(description = "남은 유효 시간 (초)", example = "3600")
    val remainingTimeSeconds: Long
)