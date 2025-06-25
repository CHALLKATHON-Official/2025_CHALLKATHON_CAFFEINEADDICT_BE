package com.challkathon.momento.domain.todo.repository

import com.challkathon.momento.domain.todo.TodoList
import com.challkathon.momento.domain.todo.entity.enums.TodoCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TodoListRepository : JpaRepository<TodoList, Long> {
    
    /**
     * 카테고리별 사용 횟수가 적은 Todo 조회
     */
    @Query("""
        SELECT t FROM TodoList t 
        WHERE t.category = :category 
        ORDER BY t.usageCount ASC, t.createdAt DESC
    """)
    fun findByCategoryOrderByUsageCountAsc(@Param("category") category: TodoCategory): List<TodoList>
    
    /**
     * AI 생성된 Todo 중 사용 횟수가 적은 것들 조회
     */
    @Query("""
        SELECT t FROM TodoList t 
        WHERE t.isAIGenerated = true 
        ORDER BY t.usageCount ASC, t.createdAt DESC 
        LIMIT :limit
    """)
    fun findAIGeneratedTodosWithLowUsage(@Param("limit") limit: Int): List<TodoList>
    
    /**
     * 특정 기간 내 생성된 Todo 조회
     */
    @Query("""
        SELECT t FROM TodoList t 
        WHERE t.generatedDate >= :startDate 
        ORDER BY t.createdAt DESC
    """)
    fun findByGeneratedDateAfter(@Param("startDate") startDate: java.time.LocalDate): List<TodoList>
}