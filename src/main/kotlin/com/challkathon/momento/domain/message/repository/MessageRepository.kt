package com.challkathon.momento.domain.message.repository

import com.challkathon.momento.domain.message.entity.Message
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

interface MessageRepository : JpaRepository<Message, Long> {

    fun findAllByUserIdAndReservedAtBetween(
        userId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Message>

}