package com.challkathon.momento.integration

import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.question.ai.QuestionGenerationManager
import com.challkathon.momento.domain.question.scheduler.DailyQuestionScheduler
import com.challkathon.momento.domain.question.service.FamilyQuestionService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SchedulerIntegrationTest {

    @MockBean
    private lateinit var questionGenerationManager: QuestionGenerationManager
    
    @MockBean
    private lateinit var familyRepository: FamilyRepository
    
    @MockBean
    private lateinit var familyQuestionService: FamilyQuestionService
    
    private lateinit var scheduler: DailyQuestionScheduler
    
    @BeforeEach
    fun setUp() {
        scheduler = DailyQuestionScheduler(
            familyRepository,
            questionGenerationManager,
            familyQuestionService
        )
    }

    @Test
    @DisplayName("스케줄러 수동 실행 - 질문 풀 생성 테스트")
    fun testManualQuestionPoolGeneration() {
        // 실제 스케줄러 로직을 수동으로 실행하여 테스트
        println("=== 새벽 2시 질문 풀 생성 스케줄러 수동 실행 ===")
        
        try {
            runBlocking {
                scheduler.generateQuestionPool()
            }
            println("✅ 질문 풀 생성 성공")
        } catch (e: Exception) {
            println("❌ 질문 풀 생성 실패: ${e.message}")
            throw e
        }
    }

    @Test
    @DisplayName("스케줄러 수동 실행 - 질문 할당 테스트")
    fun testManualQuestionAssignment() {
        // 실제 스케줄러 로직을 수동으로 실행하여 테스트
        println("=== 오전 9시 질문 할당 스케줄러 수동 실행 ===")
        
        try {
            runBlocking {
                scheduler.generateAndAssignDailyQuestions()
            }
            println("✅ 질문 생성 및 할당 성공")
        } catch (e: Exception) {
            println("❌ 질문 생성 및 할당 실패: ${e.message}")
            throw e
        }
    }

    @Test
    @DisplayName("스케줄러 수동 실행 - 만료 질문 정리 테스트")
    fun testManualExpiredQuestionCleanup() {
        println("=== 자정 만료 질문 정리 스케줄러 수동 실행 ===")
        
        try {
            scheduler.updateExpiredQuestions()
            println("✅ 만료 질문 정리 성공")
        } catch (e: Exception) {
            println("❌ 만료 질문 정리 실패: ${e.message}")
            throw e
        }
    }
}