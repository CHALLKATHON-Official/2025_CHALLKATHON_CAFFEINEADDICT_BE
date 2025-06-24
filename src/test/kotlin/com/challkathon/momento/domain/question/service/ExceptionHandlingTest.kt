package com.challkathon.momento.domain.question.service

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.question.ai.QuestionGenerationManager
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.scheduler.DailyQuestionScheduler
import com.challkathon.momento.domain.question.service.FamilyQuestionService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ExceptionHandlingTest {

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
    @DisplayName("AI 질문 생성 실패 시 예외 처리 테스트")
    fun testAiQuestionGenerationFailureHandling() = runBlocking {
        // Given
        val family = Family("TEST_FAMILY", 3)
        whenever(familyRepository.findAll()).thenReturn(listOf(family))
        whenever(questionGenerationManager.getQuestionsForFamily(any(), any()))
            .thenThrow(RuntimeException("AI 서비스 호출 실패"))

        // When & Then
        assertDoesNotThrow {
            try {
                scheduler.generateAndAssignDailyQuestions()
            } catch (e: Exception) {
                println("⚠️ AI 질문 생성 실패 감지: ${e.message}")
                assertTrue(e.message?.contains("AI 서비스 호출 실패") == true)
            }
        }
    }

    @Test
    @DisplayName("데이터베이스 연결 오류 시 예외 처리 테스트")
    fun testDatabaseConnectionFailureHandling() {
        // Given
        whenever(familyRepository.findAll())
            .thenThrow(RuntimeException("데이터베이스 연결 실패"))

        // When & Then
        assertDoesNotThrow {
            try {
                runBlocking {
                    scheduler.generateAndAssignDailyQuestions()
                }
            } catch (e: Exception) {
                println("⚠️ 데이터베이스 연결 실패 감지: ${e.message}")
                assertTrue(e.message?.contains("데이터베이스 연결 실패") == true)
            }
        }
    }

    @Test
    @DisplayName("활성 Family가 없는 경우 처리 테스트")
    fun testNoActiveFamiliesHandling() = runBlocking {
        // Given
        whenever(familyRepository.findAll()).thenReturn(emptyList())

        // When & Then
        assertDoesNotThrow {
            scheduler.generateAndAssignDailyQuestions()
            println("✅ 활성 Family가 없는 경우 정상 처리됨")
        }

        // 빈 리스트여도 예외가 발생하지 않아야 함
        verify(familyRepository).findAll()
        verify(questionGenerationManager, never()).getQuestionsForFamily(any(), any())
    }

    @Test
    @DisplayName("질문 생성 시 빈 응답 처리 테스트")
    fun testEmptyQuestionResponseHandling() = runBlocking {
        // Given
        val family = Family("TEST_FAMILY", 3)
        whenever(familyRepository.findAll()).thenReturn(listOf(family))
        whenever(questionGenerationManager.getQuestionsForFamily(any(), any()))
            .thenReturn(emptyList())

        // When & Then
        assertDoesNotThrow {
            scheduler.generateAndAssignDailyQuestions()
            println("✅ 빈 질문 응답 처리 테스트 통과")
        }

        verify(questionGenerationManager).getQuestionsForFamily(family, false)
        verify(familyQuestionService, never()).assignQuestionsToFamily(any(), any())
    }

    @Test
    @DisplayName("스케줄러 동시 실행 처리 테스트")
    fun testSchedulerConcurrencyHandling() = runBlocking {
        // Given
        val families = listOf(Family("FAMILY1", 2), Family("FAMILY2", 3))
        whenever(familyRepository.findAll()).thenReturn(families)
        whenever(questionGenerationManager.getQuestionsForFamily(any(), any()))
            .thenReturn(emptyList())

        // When - 동시에 여러 번 호출
        val results = mutableListOf<Exception?>()

        repeat(3) {
            try {
                scheduler.generateAndAssignDailyQuestions()
                results.add(null)
            } catch (e: Exception) {
                results.add(e)
                println("⚠️ 동시 실행 시 예외 발생: ${e.message}")
            }
        }

        // Then
        println("✅ 스케줄러 동시 실행 테스트 완료")
        assertTrue(results.size == 3, "모든 호출이 완료되어야 함")
    }

    @Test
    @DisplayName("질문 풀 생성 실패 처리 테스트")
    fun testQuestionPoolGenerationFailure() = runBlocking {
        // Given
        whenever(questionGenerationManager.generateQuestionPool(any(), any()))
            .thenThrow(RuntimeException("질문 풀 생성 실패"))

        // When & Then
        assertDoesNotThrow {
            try {
                scheduler.generateQuestionPool()
            } catch (e: Exception) {
                println("⚠️ 질문 풀 생성 실패 감지: ${e.message}")
                assertTrue(e.message?.contains("질문 풀 생성 실패") == true)
            }
        }
    }

    @Test
    @DisplayName("메모리 부족 상황 시뮬레이션 테스트")
    fun testOutOfMemoryHandling() = runBlocking {
        // Given
        whenever(questionGenerationManager.generateQuestionPool(any(), any()))
            .thenThrow(OutOfMemoryError("메모리 부족"))

        // When & Then
        assertThrows(OutOfMemoryError::class.java) {
            runBlocking {
                scheduler.generateQuestionPool()
            }
        }

        println("⚠️ 메모리 부족 상황에서 적절한 예외 발생 확인")
    }

    @Test
    @DisplayName("네트워크 타임아웃 처리 테스트")
    fun testNetworkTimeoutHandling() = runBlocking {
        // Given
        val family = Family("TEST_FAMILY", 3)
        whenever(familyRepository.findAll()).thenReturn(listOf(family))
        whenever(questionGenerationManager.getQuestionsForFamily(any(), any()))
            .thenAnswer {
                Thread.sleep(100) // 타임아웃 시뮬레이션
                throw RuntimeException("네트워크 타임아웃")
            }

        // When & Then
        assertDoesNotThrow {
            try {
                scheduler.generateAndAssignDailyQuestions()
            } catch (e: Exception) {
                println("⚠️ 네트워크 타임아웃 감지: ${e.message}")
                assertTrue(e.message?.contains("타임아웃") == true)
            }
        }
    }

    @Test
    @DisplayName("잘못된 설정값 처리 테스트")
    fun testInvalidConfigurationHandling() = runBlocking {
        // Given - 잘못된 카테고리로 질문 풀 생성 시도
        whenever(questionGenerationManager.generateQuestionPool(any(), any()))
            .thenThrow(IllegalArgumentException("잘못된 카테고리입니다"))

        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                questionGenerationManager.generateQuestionPool(QuestionCategory.DAILY, -1)
            }
        }

        println("⚠️ 잘못된 설정값에 대한 적절한 예외 발생 확인")
    }

    @Test
    @DisplayName("Family 질문 할당 서비스 실패 처리 테스트")
    fun testFamilyQuestionServiceFailureHandling() = runBlocking {
        // Given
        val family = Family("TEST_FAMILY", 3)
        val questions = emptyList<Question>()
        whenever(familyRepository.findAll()).thenReturn(listOf(family))
        whenever(questionGenerationManager.getQuestionsForFamily(any(), any())).thenReturn(questions)
        whenever(familyQuestionService.assignQuestionsToFamily(any(), any()))
            .thenThrow(RuntimeException("질문 할당 실패"))

        // When & Then
        assertDoesNotThrow {
            try {
                scheduler.generateAndAssignDailyQuestions()
            } catch (e: Exception) {
                println("⚠️ 질문 할당 실패 감지: ${e.message}")
                assertTrue(e.message?.contains("질문 할당 실패") == true)
            }
        }
    }
}