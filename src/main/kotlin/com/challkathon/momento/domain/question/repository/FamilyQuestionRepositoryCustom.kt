package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

interface FamilyQuestionRepositoryCustom {
    fun findTodayQuestionsByFamilyId(familyId: Long): List<FamilyQuestion>
    fun findByFamilyIdAndDateRange(familyId: Long, startDate: LocalDateTime, endDate: LocalDateTime): List<FamilyQuestion>
    fun countAnsweredQuestionsSince(familyId: Long, since: LocalDateTime): Long
    fun findExpiredQuestions(status: com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus): List<FamilyQuestion>
    fun findByFamilyIdOrderByAssignedAtDesc(familyId: Long): List<FamilyQuestion>
    fun findRecentByFamilyId(familyId: Long, pageable: Pageable): List<FamilyQuestion>
}