package com.challkathon.momento.auth.dto.response

data class OAuthFailureResponse(
    val success: Boolean,
    val error: Map<String, String>
)