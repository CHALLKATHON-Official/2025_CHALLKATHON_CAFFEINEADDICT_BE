package com.challkathon.momento.domain.question.repository

import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface QuestionRepository : JpaRepository<Question, Long>, QuestionRepositoryCustom {
    fun findByCategoryAndIsAIGeneratedOrderByUsageCountAscCreatedAtDesc(category: QuestionCategory, isAIGenerated: Boolean): List<Question>
    fun findByGeneratedDateGreaterThanEqualAndIsAIGenerated(startDate: LocalDate, isAIGenerated: Boolean): List<Question>
}
