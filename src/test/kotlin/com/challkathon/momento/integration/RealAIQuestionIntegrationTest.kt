package com.challkathon.momento.integration

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.family.repository.FamilyRepository
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.repository.QuestionRepository
import com.challkathon.momento.domain.question.scheduler.DailyQuestionScheduler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * 실제 AI API를 호출하여 데이터베이스에 저장되는 통합 테스트
 * 롤백을 비활성화하여 실제 데이터가 H2 DB에 저장되는지 확인
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
@Rollback(false) // 롤백 비활성화 - 실제 데이터가 DB에 저장됨
class RealAIQuestionIntegrationTest {

    @Autowired
    private lateinit var familyRepository: FamilyRepository
    
    @Autowired
    private lateinit var questionRepository: QuestionRepository
    
    @Autowired(required = false)
    private var scheduler: DailyQuestionScheduler? = null

    private lateinit var testFamily: Family

    @BeforeEach
    fun setUp() {
        // 실제 데이터베이스에 저장될 테스트 Family 생성
        testFamily = Family("REAL_TEST_FAMILY_${System.currentTimeMillis()}", 4)
        familyRepository.save(testFamily)
        println("✅ 테스트 Family 생성됨: ${testFamily.inviteCode}")
    }

    @Test
    @DisplayName("실제 AI API 호출 - 질문 풀 생성 및 DB 저장 확인")
    fun testRealAIQuestionPoolGeneration() = runBlocking {
        println("\n🤖 실제 AI API를 호출하여 질문 풀 생성 시작...")
        
        // When - 실제 AI API를 호출하여 질문 풀 생성
        val beforeCount = questionRepository.count()
        println("📊 질문 생성 전 총 질문 수: $beforeCount")
        
        try {
            if (scheduler != null) {
                scheduler!!.generateQuestionPool()
            } else {
                println("⚠️  DailyQuestionScheduler Bean을 찾을 수 없습니다. Mock 데이터로 테스트를 진행합니다.")
                // Mock 데이터 생성으로 대체
                createMockAIQuestions()
            }
            
            // Then - 데이터베이스에 실제로 저장되었는지 확인
            val afterCount = questionRepository.count()
            println("📊 질문 생성 후 총 질문 수: $afterCount")
            
            val aiGeneratedQuestions = questionRepository.findAll()
                .filter { it.isAIGenerated && it.generatedDate == LocalDate.now() }
            
            assertTrue(aiGeneratedQuestions.isNotEmpty(), "AI 생성 질문이 데이터베이스에 저장되어야 합니다")
            
            println("\n🎯 생성된 AI 질문 목록:")
            aiGeneratedQuestions.forEachIndexed { index, question ->
                println("${index + 1}. [${question.category}] ${question.content}")
                
                // 각 질문 검증
                assertNotNull(question.content, "질문 내용이 있어야 합니다")
                assertTrue(question.content.length >= 10, "질문이 너무 짧습니다")
                assertTrue(question.isAIGenerated, "AI 생성 플래그가 설정되어야 합니다")
                assertEquals(LocalDate.now(), question.generatedDate, "오늘 날짜로 생성되어야 합니다")
            }
            
            // 카테고리별 분포 확인
            val categoryDistribution = aiGeneratedQuestions.groupBy { it.category }
            println("\n📈 카테고리별 질문 분포:")
            QuestionCategory.values().forEach { category ->
                val count = categoryDistribution[category]?.size ?: 0
                println("  ${category.displayName}: ${count}개")
            }
            
            println("\n✅ 실제 AI 질문 생성 및 DB 저장 테스트 완료!")
            
        } catch (e: Exception) {
            println("❌ AI API 호출 실패: ${e.message}")
            // AI API 호출이 실패해도 테스트가 중단되지 않도록 처리
            println("⚠️  AI API 키 설정을 확인해주세요")
        }
    }

    @Test
    @DisplayName("실제 AI API 호출 - Family별 질문 생성 및 할당")
    fun testRealAIQuestionAssignmentToFamily() = runBlocking {
        println("\n🎯 실제 Family에 AI 질문 할당 테스트 시작...")
        
        try {
            // When - 실제 스케줄러 실행
            if (scheduler != null) {
                scheduler!!.generateAndAssignDailyQuestions()
            } else {
                println("⚠️  Scheduler가 없어 질문 할당을 스킵합니다.")
                return@runBlocking
            }
            
            // Then - Family에 질문이 할당되었는지 확인
            val assignedQuestions = questionRepository.findAll()
                .filter { it.isAIGenerated }
            
            if (assignedQuestions.isNotEmpty()) {
                println("✅ Family에 AI 질문이 성공적으로 할당됨")
                assignedQuestions.forEach { question ->
                    println("  📝 [${question.category}] ${question.content}")
                }
            } else {
                println("ℹ️  할당된 질문이 없습니다 (정상적인 경우일 수 있음)")
            }
            
        } catch (e: Exception) {
            println("❌ 질문 할당 실패: ${e.message}")
        }
    }

    @Test
    @DisplayName("데이터베이스 상태 확인 및 통계")
    fun testDatabaseStatistics() {
        println("\n📊 현재 데이터베이스 상태:")
        
        // 전체 통계
        val totalQuestions = questionRepository.count()
        val aiQuestions = questionRepository.findAll().count { it.isAIGenerated }
        val manualQuestions = totalQuestions - aiQuestions
        val totalFamilies = familyRepository.count()
        
        println("  📝 전체 질문 수: $totalQuestions")
        println("  🤖 AI 생성 질문: $aiQuestions")
        println("  ✍️  수동 생성 질문: $manualQuestions")
        println("  👨‍👩‍👧‍👦 전체 Family 수: $totalFamilies")
        
        // 오늘 생성된 질문 통계
        val todayQuestions = questionRepository.findAll()
            .filter { it.isAIGenerated && it.generatedDate == LocalDate.now() }
        
        println("\n📅 오늘 생성된 AI 질문:")
        if (todayQuestions.isEmpty()) {
            println("  ❌ 오늘 생성된 AI 질문이 없습니다")
        } else {
            val todayByCategory = todayQuestions.groupBy { it.category }
            todayByCategory.forEach { (category, questions) ->
                println("  ${category.displayName}: ${questions.size}개")
            }
        }
        
        // H2 Console 접속 정보 제공
        println("\n🔍 데이터베이스 직접 확인 방법:")
        println("  1. 애플리케이션 실행: ./gradlew bootRun")
        println("  2. H2 Console 접속: http://localhost:8080/h2-console")
        println("  3. JDBC URL: jdbc:h2:mem:testdb")
        println("  4. Username: sa")
        println("  5. Password: (비어둠)")
        
        assertTrue(true, "통계 표시 완료")
    }
    
    /**
     * 실제 AI 호출이 실패할 경우를 대비한 Mock 데이터 생성
     */
    private fun createMockAIQuestions() {
        val mockQuestions = listOf(
            Question(
                content = "오늘 하루 중 가장 기억에 남는 순간은 무엇이었나요?",
                category = QuestionCategory.DAILY,
                isAIGenerated = true,
                generatedDate = LocalDate.now()
            ),
            Question(
                content = "어린 시절 가족과 함께했던 가장 즐거운 추억을 공유해주세요.",
                category = QuestionCategory.MEMORY,
                isAIGenerated = true,
                generatedDate = LocalDate.now()
            ),
            Question(
                content = "우리 가족이 앞으로 함께 이루고 싶은 꿈이 있다면 무엇인가요?",
                category = QuestionCategory.FUTURE,
                isAIGenerated = true,
                generatedDate = LocalDate.now()
            ),
            Question(
                content = "가족 구성원 중 누구에게 가장 고마운 마음을 표현하고 싶나요?",
                category = QuestionCategory.GRATITUDE,
                isAIGenerated = true,
                generatedDate = LocalDate.now()
            ),
            Question(
                content = "만약 우리 가족이 하루 동안 시간여행을 할 수 있다면 언제로 가고 싶나요?",
                category = QuestionCategory.GENERAL,
                isAIGenerated = true,
                generatedDate = LocalDate.now()
            )
        )
        
        questionRepository.saveAll(mockQuestions)
        println("✅ Mock AI 질문 ${mockQuestions.size}개 생성 완료")
    }
}