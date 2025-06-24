package com.challkathon.momento.domain.question.scheduler

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.question.ai.QuestionGenerationManager
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.service.FamilyQuestionService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import java.time.LocalDate

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = ["scheduler.enabled=true"])
class DailyQuestionSchedulerTest {

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
    @DisplayName("매일 오전 9시 질문 생성 및 할당 스케줄러 테스트")
    fun testDailyQuestionAssignment() = runBlocking {
        // Given
        val mockFamilies = listOf(
            Family("FAMILY001", 3),
            Family("FAMILY002", 4)
        )
        whenever(familyRepository.findAll()).thenReturn(mockFamilies)
        whenever(questionGenerationManager.getQuestionsForFamily(any(), any())).thenReturn(
            listOf(Question("테스트 질문", QuestionCategory.DAILY, true, LocalDate.now()))
        )
        whenever(familyQuestionService.assignQuestionsToFamily(any(), any())).thenAnswer { }

        // When
        scheduler.generateAndAssignDailyQuestions()

        // Then
        verify(familyRepository).findAll()
        verify(questionGenerationManager, times(2)).getQuestionsForFamily(any(), eq(false))
        verify(familyQuestionService, times(2)).assignQuestionsToFamily(any(), any())
    }

    @Test
    @DisplayName("매일 새벽 2시 질문 풀 생성 스케줄러 테스트")
    fun testDailyQuestionPoolGeneration() = runBlocking {
        // Given
        val categories = QuestionCategory.values()
        
        // When
        scheduler.generateQuestionPool()

        // Then
        categories.forEach { category ->
            verify(questionGenerationManager).generateQuestionPool(category, 20)
        }
    }

    @Test
    @DisplayName("매일 자정 만료 질문 처리 스케줄러 테스트")
    fun testExpiredQuestionCleanup() {
        // When
        scheduler.updateExpiredQuestions()

        // Then
        verify(familyQuestionService).updateExpiredQuestions()
    }
}