package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.Answer
import com.challkathon.momento.domain.user.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AnswerRepository : JpaRepository<Answer, Long> {
    
    @Query("""
        SELECT a FROM Answer a 
        JOIN FETCH a.familyQuestion fq 
        JOIN FETCH fq.question q 
        WHERE a.user = :user 
        ORDER BY a.createdAt DESC
    """)
    fun findByUserOrderByCreatedAtDesc(@Param("user") user: User): List<Answer>
    
    @Query("""
        SELECT a FROM Answer a 
        JOIN FETCH a.familyQuestion fq 
        JOIN FETCH fq.question q 
        WHERE a.user = :user 
        ORDER BY a.createdAt DESC 
        LIMIT :limit
    """)
    fun findRecentAnswersByUser(@Param("user") user: User, @Param("limit") limit: Int): List<Answer>
}