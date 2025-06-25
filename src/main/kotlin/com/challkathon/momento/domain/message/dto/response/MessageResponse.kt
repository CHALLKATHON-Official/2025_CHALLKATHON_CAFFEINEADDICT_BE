package com.challkathon.momento.domain.message.dto.response

import java.time.LocalDate

data class MessageResponse(
    val id: Long,
    val content: String,
    val imageUrl: String,
)