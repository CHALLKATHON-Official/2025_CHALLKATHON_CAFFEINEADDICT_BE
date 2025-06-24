package com.challkathon.momento.domain.question.ai

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.question.dto.MinimalFamilyContext
import com.challkathon.momento.domain.question.dto.SpecialContext
import com.challkathon.momento.domain.question.entity.enums.ActivityLevel
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.repository.FamilyQuestionRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class FamilyContextAnalyzer(
    private val familyQuestionRepository: FamilyQuestionRepository
) {
    
    @Cacheable(cacheNames = ["familyContext"], key = "#family.id")
    fun analyzeFamily(family: Family): MinimalFamilyContext {
        val activityLevel = calculateActivityLevel(family)
        val preferredCategory = determinePreferredCategory(family)
        val specialContext = checkSpecialContext(family)
        
        return MinimalFamilyContext(
            familyId = family.id,
            memberCount = family.count,
            activityLevel = activityLevel,
            preferredCategory = preferredCategory,
            specialContext = specialContext
        )
    }
    
    private fun calculateActivityLevel(family: Family): ActivityLevel {
        val recentAnswers = familyQuestionRepository.countAnsweredQuestionsSince(
            familyId = family.id,
            since = LocalDateTime.now().minusDays(7)
        )
        
        val avgAnswersPerDay = recentAnswers / 7.0
        
        return when {
            avgAnswersPerDay >= 2 -> ActivityLevel.HIGH
            avgAnswersPerDay >= 1 -> ActivityLevel.MEDIUM
            else -> ActivityLevel.LOW
        }
    }
    
    private fun determinePreferredCategory(family: Family): QuestionCategory? {
        // 가족이 가장 많이 답변한 카테고리 분석
        // 실제 구현에서는 답변률이 높은 카테고리를 찾아야 함
        // 여기서는 단순화를 위해 null 반환
        return null
    }
    
    private fun checkSpecialContext(family: Family): SpecialContext? {
        val today = LocalDate.now()
        
        // 특별한 날 체크 (예: 크리스마스, 새해 등)
        return when {
            today.monthValue == 12 && today.dayOfMonth in 24..25 -> 
                SpecialContext("CHRISTMAS", "크리스마스")
            today.monthValue == 1 && today.dayOfMonth == 1 ->
                SpecialContext("NEW_YEAR", "새해")
            today.dayOfWeek.value == 7 -> // 일요일
                SpecialContext("WEEKEND", "주말")
            else -> null
        }
    }
}
