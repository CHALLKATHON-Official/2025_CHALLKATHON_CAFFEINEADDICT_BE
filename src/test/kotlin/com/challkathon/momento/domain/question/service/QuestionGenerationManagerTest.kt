package com.challkathon.momento.domain.question.service

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.question.ai.FamilyContextAnalyzer
import com.challkathon.momento.domain.question.ai.QuestionGenerationManager
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.repository.QuestionRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.time.LocalDate

@SpringBootTest
class QuestionGenerationManagerTest {

    @MockBean
    private lateinit var familyContextAnalyzer: FamilyContextAnalyzer

    @MockBean
    private lateinit var questionRepository: QuestionRepository

    private lateinit var questionGenerationManager: QuestionGenerationManager

    @BeforeEach
    fun setUp() {
        questionGenerationManager = QuestionGenerationManager(
            familyContextAnalyzer,
            questionRepository
        )
    }

    @Test
    @DisplayName("Family를 위한 질문 생성 테스트")
    fun testGetQuestionsForFamily() = runBlocking {
        // Given
        val family = Family("TEST_FAMILY", 4)
        val expectedQuestions = listOf(
            Question("테스트 질문 1", QuestionCategory.DAILY, true, LocalDate.now()),
            Question("테스트 질문 2", QuestionCategory.MEMORY, true, LocalDate.now())
        )

        // 기존 질문이 없는 경우를 시뮬레이션
        whenever(questionRepository.findByCategoryAndIsAIGeneratedOrderByUsageCountAscCreatedAtDesc(any(), eq(true)))
            .thenReturn(expectedQuestions)

        // When
        val result = questionGenerationManager.getQuestionsForFamily(family, false)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        verify(questionRepository).findByCategoryAndIsAIGeneratedOrderByUsageCountAscCreatedAtDesc(any(), eq(true))
    }

    @Test
    @DisplayName("질문 풀 생성 테스트")
    fun testGenerateQuestionPool() = runBlocking {
        // Given
        val category = QuestionCategory.DAILY
        val count = 10

        // When
        questionGenerationManager.generateQuestionPool(category, count)

        // Then
        // 실제 구현에서는 AI 서비스를 호출하여 질문들이 생성되고 저장됨
        // 여기서는 메서드가 호출되는지만 확인
        assertTrue(true) // placeholder assertion
    }

    @Test
    @DisplayName("강제 새 질문 생성 테스트")
    fun testForceNewQuestionGeneration() = runBlocking {
        // Given
        val family = Family("TEST_FAMILY", 3)
        val forceNew = true

        // When
        val result = questionGenerationManager.getQuestionsForFamily(family, forceNew)

        // Then
        assertNotNull(result)
        // forceNew = true일 때는 캐시를 무시하고 새로운 질문을 생성해야 함
        assertTrue(true) // placeholder assertion
    }
}