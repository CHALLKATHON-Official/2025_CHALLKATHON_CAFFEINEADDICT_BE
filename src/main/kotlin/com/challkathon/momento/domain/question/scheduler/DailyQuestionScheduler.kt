package com.challkathon.momento.domain.question.scheduler

import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.question.ai.QuestionGenerationManager
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.service.FamilyQuestionService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val log = KotlinLogging.logger {}

@Component
@EnableScheduling
@ConditionalOnProperty(
    prefix = "scheduler.question-generation",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class DailyQuestionScheduler(
    private val familyRepository: FamilyRepository,
    private val questionGenerationManager: QuestionGenerationManager,
    private val familyQuestionService: FamilyQuestionService
) {
    
    /**
     * 매일 오전 9시에 가족별 질문 생성 및 할당
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    fun generateAndAssignDailyQuestions() = runBlocking {
        log.info { "Starting daily question generation at ${LocalDateTime.now()}" }
        
        try {
            val activeFamilies = familyRepository.findActiveFamilies()
            log.info { "Found ${activeFamilies.size} active families" }
            
            var successCount = 0
            var failureCount = 0
            
            activeFamilies.forEach { family ->
                try {
                    // 1. AI로 질문 생성
                    val questions = questionGenerationManager.getQuestionsForFamily(family)
                    
                    // 2. 가족에게 질문 할당
                    val assignedQuestions = familyQuestionService.assignQuestionsToFamily(
                        family = family,
                        questions = questions
                    )
                    
                    log.info { "Assigned ${assignedQuestions.size} questions to family ${family.id}" }
                    successCount++
                    
                } catch (e: Exception) {
                    log.error(e) { "Failed to process family ${family.id}" }
                    failureCount++
                }
            }
            
            log.info { 
                "Daily question generation completed. " +
                "Success: $successCount, Failure: $failureCount" 
            }
            
        } catch (e: Exception) {
            log.error(e) { "Critical error in daily question generation" }
        }
    }
    
    /**
     * 매일 새벽 2시에 질문 풀 생성
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    fun generateQuestionPool() = runBlocking {
        log.info { "Starting question pool generation at ${LocalDateTime.now()}" }
        
        try {
            QuestionCategory.values().forEach { category ->
                try {
                    questionGenerationManager.generateQuestionPool(category, count = 20)
                    log.info { "Generated question pool for category: $category" }
                } catch (e: Exception) {
                    log.error(e) { "Failed to generate pool for category: $category" }
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Critical error in question pool generation" }
        }
    }
    
    /**
     * 매일 자정에 만료된 질문 상태 업데이트
     */
    @Scheduled(cron = "0 0 0 * * *")
    fun updateExpiredQuestions() {
        log.info { "Updating expired questions at ${LocalDateTime.now()}" }
        
        try {
            familyQuestionService.updateExpiredQuestions()
        } catch (e: Exception) {
            log.error(e) { "Failed to update expired questions" }
        }
    }
}
