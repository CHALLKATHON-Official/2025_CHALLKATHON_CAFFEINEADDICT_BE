package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.mapping.UserQuestion
import com.challkathon.momento.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserQuestionRepository : JpaRepository<UserQuestion, Long> {
    
    fun findByUserOrderByCreatedAtDesc(user: User): List<UserQuestion>
    
    @Query("""
        SELECT uq FROM UserQuestion uq
        JOIN FETCH uq.question q
        WHERE uq.user = :user
        AND uq.createdAt >= :startTime
        ORDER BY uq.createdAt DESC
    """)
    fun findRecentQuestionsByUser(
        @Param("user") user: User,
        @Param("startTime") startTime: LocalDateTime
    ): List<UserQuestion>
    
    @Query("""
        SELECT COUNT(uq) > 0 FROM UserQuestion uq
        JOIN uq.question q
        WHERE uq.user = :user
        AND q.content = :content
        AND uq.createdAt >= :startTime
    """)
    fun existsSimilarQuestionRecently(
        @Param("user") user: User,
        @Param("content") content: String,
        @Param("startTime") startTime: LocalDateTime
    ): Boolean
}
