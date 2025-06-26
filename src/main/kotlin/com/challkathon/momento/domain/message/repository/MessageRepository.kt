package com.challkathon.momento.domain.message.repository

import com.challkathon.momento.domain.message.entity.Message
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate
import java.time.LocalDateTime

interface MessageRepository : JpaRepository<Message, Long> {

    fun findAllByUserIdAndReservedAtBetween(
        userId: Long,
        start: LocalDateTime,
        end: LocalDateTime
    ): List<Message>

    @Query(
        "SELECT m FROM Message m WHERE m.user.id = :userId AND FUNCTION('DATE', m.reservedAt) = :date"
    )
    fun findByUserIdAndDate(
        @Param("userId") userId: Long,
        @Param("date") date: LocalDate
    ): List<Message>

}