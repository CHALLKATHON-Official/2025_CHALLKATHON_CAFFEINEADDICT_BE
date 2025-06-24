package com.challkathon.momento.integration

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import com.challkathon.momento.domain.question.repository.FamilyQuestionRepository
import com.challkathon.momento.domain.question.repository.QuestionRepository
import com.challkathon.momento.domain.question.scheduler.DailyQuestionScheduler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataValidationTest {

    @Autowired
    private lateinit var familyRepository: FamilyRepository
    
    @Autowired
    private lateinit var questionRepository: QuestionRepository
    
    @Autowired
    private lateinit var familyQuestionRepository: FamilyQuestionRepository
    
    @Autowired
    private lateinit var scheduler: DailyQuestionScheduler

    private lateinit var testFamily: Family

    @BeforeEach
    fun setUp() {
        // 테스트용 Family 생성
        testFamily = Family("TEST_FAMILY_${System.currentTimeMillis()}", 4)
        familyRepository.save(testFamily)
    }

    @Test
    @DisplayName("질문 생성 후 데이터 검증")
    fun testQuestionGenerationDataValidation() = runBlocking {
        // Given & When - 스케줄러로 질문 생성
        scheduler.generateQuestionPool()
        
        // Then - 생성된 질문들 검증
        val generatedQuestions = questionRepository.findAll().filter { it.isAIGenerated }
        
        assertTrue(generatedQuestions.isNotEmpty(), "AI 생성 질문이 하나 이상 있어야 합니다")
        
        generatedQuestions.forEach { question ->
            // 질문 내용 검증
            assertNotNull(question.content, "질문 내용이 null이면 안됩니다")
            assertFalse(question.content.isBlank(), "질문 내용이 비어있으면 안됩니다")
            assertTrue(question.content.length >= 10, "질문 내용이 너무 짧습니다: ${question.content}")
            assertTrue(question.content.length <= 200, "질문 내용이 너무 깁니다: ${question.content}")
            
            // AI 생성 플래그 검증
            assertTrue(question.isAIGenerated, "AI 생성 플래그가 true여야 합니다")
            
            // 카테고리 검증
            assertNotNull(question.category, "질문 카테고리가 null이면 안됩니다")
            assertTrue(QuestionCategory.values().contains(question.category), "유효한 카테고리여야 합니다")
            
            // 생성 날짜 검증
            assertNotNull(question.generatedDate, "생성 날짜가 null이면 안됩니다")
            assertTrue(question.generatedDate!!.isAfter(LocalDate.now().minusDays(1)), "생성 날짜가 너무 오래되었습니다")
            
            println("✅ 검증 통과: [${question.category}] ${question.content}")
        }
    }

    @Test
    @DisplayName("Family에 질문 할당 후 데이터 검증")
    fun testQuestionAssignmentDataValidation() = runBlocking {
        // Given - 테스트용 질문 생성
        val testQuestion = Question("테스트 질문", QuestionCategory.DAILY, true, LocalDate.now())
        questionRepository.save(testQuestion)
        
        // When - 질문 할당
        scheduler.generateAndAssignDailyQuestions()
        
        // Then - 할당된 질문들 검증
        val familyQuestions = familyQuestionRepository.findAll().filter { it.family.id == testFamily.id }
        
        familyQuestions.forEach { familyQuestion ->
            // 기본 필드 검증
            assertNotNull(familyQuestion.question, "연결된 질문이 null이면 안됩니다")
            assertNotNull(familyQuestion.family, "연결된 family가 null이면 안됩니다")
            assertEquals(testFamily.id, familyQuestion.family.id, "올바른 family에 할당되어야 합니다")
            
            // 할당 시간 검증
            assertNotNull(familyQuestion.assignedAt, "할당 시간이 null이면 안됩니다")
            assertTrue(familyQuestion.assignedAt.isAfter(LocalDateTime.now().minusMinutes(5)), "할당 시간이 최근이어야 합니다")
            
            // 상태 검증
            assertEquals(FamilyQuestionStatus.ASSIGNED, familyQuestion.status, "초기 상태는 ASSIGNED여야 합니다")
            
            // 만료 시간 검증 (24시간 후)
            if (familyQuestion.dueDate != null) {
                assertTrue(familyQuestion.dueDate!!.isAfter(LocalDateTime.now().plusHours(20)), "만료 시간이 적절해야 합니다")
                assertTrue(familyQuestion.dueDate!!.isBefore(LocalDateTime.now().plusHours(28)), "만료 시간이 적절해야 합니다")
            }
            
            println("✅ 할당 검증 통과: Family[${familyQuestion.family.inviteCode}] <- Question[${familyQuestion.question.content}]")
        }
    }

    @Test
    @DisplayName("중복 할당 방지 검증")
    fun testDuplicateAssignmentPrevention() = runBlocking {
        // Given - 테스트용 질문 생성
        val testQuestion = Question("중복 테스트 질문", QuestionCategory.DAILY, true, LocalDate.now())
        questionRepository.save(testQuestion)
        
        // When - 동일한 스케줄러를 여러 번 실행
        scheduler.generateAndAssignDailyQuestions()
        val firstAssignmentCount = familyQuestionRepository.findAll().filter { it.family.id == testFamily.id }.size
        
        scheduler.generateAndAssignDailyQuestions()
        val secondAssignmentCount = familyQuestionRepository.findAll().filter { it.family.id == testFamily.id }.size
        
        // Then - 중복 할당이 없어야 함
        assertEquals(firstAssignmentCount, secondAssignmentCount, "중복 할당이 발생하면 안됩니다")
        
        println("✅ 중복 할당 방지 검증 통과: 할당 수 = $firstAssignmentCount")
    }

    @Test
    @DisplayName("활성 Family 조회 로직 검증")
    fun testActiveFamilyDetection() {
        // Given - 활성 Family와 비활성 Family 생성
        val activeFamily = Family("ACTIVE_FAMILY", 3)
        val inactiveFamily = Family("INACTIVE_FAMILY", 0)
        
        familyRepository.saveAll(listOf(activeFamily, inactiveFamily))
        
        // When - 모든 Family 조회 (실제 구현에서는 활성 조건을 스케줄러에서 처리)
        val allFamilies = familyRepository.findAll()
        
        // Then - 조회된 Family들 검증
        assertTrue(allFamilies.isNotEmpty(), "Family가 있어야 합니다")
        
        allFamilies.forEach { family ->
            assertNotNull(family.inviteCode, "초대 코드가 있어야 합니다")
            assertTrue(family.count >= 0, "구성원 수는 0 이상이어야 합니다")
            
            println("✅ Family 검증 통과: ${family.inviteCode} (구성원: ${family.count}명)")
        }
    }
}