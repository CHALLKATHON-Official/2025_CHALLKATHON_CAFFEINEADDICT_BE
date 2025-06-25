package com.challkathon.momento.domain.todo.repository

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.todo.mapping.FamilyTodoList
import com.challkathon.momento.domain.todo.entity.enums.FamilyTodoStatus
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FamilyTodoListRepository : JpaRepository<FamilyTodoList, Long> {
    
    /**
     * 가족별 Todo 목록 조회 (최신순)
     */
    @Query("""
        SELECT ftl FROM FamilyTodoList ftl 
        JOIN FETCH ftl.todoList 
        WHERE ftl.family = :family 
        ORDER BY ftl.assignedAt DESC
    """)
    fun findByFamilyOrderByAssignedAtDesc(@Param("family") family: Family): List<FamilyTodoList>
    
    /**
     * 가족별 상태에 따른 Todo 목록 조회
     */
    @Query("""
        SELECT ftl FROM FamilyTodoList ftl 
        JOIN FETCH ftl.todoList 
        WHERE ftl.family = :family AND ftl.status = :status 
        ORDER BY ftl.assignedAt DESC
    """)
    fun findByFamilyAndStatusOrderByAssignedAtDesc(
        @Param("family") family: Family, 
        @Param("status") status: FamilyTodoStatus
    ): List<FamilyTodoList>
    
    /**
     * 가족이 오늘 할당받은 Todo 개수 조회
     */
    @Query("""
        SELECT COUNT(ftl) FROM FamilyTodoList ftl 
        WHERE ftl.family = :family 
        AND DATE(ftl.assignedAt) = CURRENT_DATE
    """)
    fun countTodayAssignedTodos(@Param("family") family: Family): Long
    
    /**
     * 만료된 Todo 조회
     */
    @Query("""
        SELECT ftl FROM FamilyTodoList ftl 
        WHERE ftl.dueDate < CURRENT_TIMESTAMP 
        AND ftl.status != :completedStatus
    """)
    fun findOverdueTodos(@Param("completedStatus") completedStatus: FamilyTodoStatus = FamilyTodoStatus.COMPLETED): List<FamilyTodoList>
    
    /**
     * 가족별 최신 Todo 목록 조회 (제한된 개수)
     */
    @Query("""
        SELECT ftl FROM FamilyTodoList ftl 
        JOIN FETCH ftl.todoList 
        WHERE ftl.family = :family 
        ORDER BY ftl.assignedAt DESC
    """)
    fun findRecentByFamily(@Param("family") family: Family, pageable: Pageable): List<FamilyTodoList>
}