package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.Answer
import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
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
    
    /**
     * 특정 FamilyQuestion과 User로 답변 조회 (중복 답변 체크용)
     */
    fun findByFamilyQuestionAndUser(familyQuestion: FamilyQuestion, user: User): Answer?
    
    /**
     * 특정 FamilyQuestion의 모든 답변 조회 (생성일시 순)
     */
    @Query("""
        SELECT a FROM Answer a 
        JOIN FETCH a.user 
        WHERE a.familyQuestion = :familyQuestion 
        ORDER BY a.createdAt ASC
    """)
    fun findByFamilyQuestionOrderByCreatedAtAsc(@Param("familyQuestion") familyQuestion: FamilyQuestion): List<Answer>
}