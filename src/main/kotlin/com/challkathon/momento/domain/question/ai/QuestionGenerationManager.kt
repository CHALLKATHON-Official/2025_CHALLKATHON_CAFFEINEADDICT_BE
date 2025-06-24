package com.challkathon.momento.domain.question.ai

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.repository.QuestionRepository
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.cache.annotation.Cacheable
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDate

private val log = KotlinLogging.logger {}

@Service
@Transactional
class QuestionGenerationManager(
    private val contextAnalyzer: FamilyContextAnalyzer,
    private val questionRepository: QuestionRepository
) {
    @Value("\${question.assignment.count-per-family:1}")
    private var questionsPerFamily: Int = 1
    @Autowired(required = false)
    private var assistantService: AssistantService? = null
    
    @Autowired(required = false)
    private var redisTemplate: RedisTemplate<String, Any>? = null
    
    /**
     * 계층적 질문 생성 전략
     * 1. 캐시 확인
     * 2. 사전 생성 풀에서 선택
     * 3. AI 생성 (필요시만)
     */
    suspend fun getQuestionsForFamily(
        family: Family,
        forceNew: Boolean = false
    ): List<Question> {
        val context = contextAnalyzer.analyzeFamily(family)
        
        // Level 1: 캐시된 질문
        if (!forceNew) {
            getCachedQuestions(family.id, context.hashCode())?.let {
                log.info { "Cache hit for family ${family.id}" }
                return it
            }
        }
        
        // Level 2: 사전 생성 풀
        val category = context.preferredCategory ?: QuestionCategory.GENERAL
        val poolQuestions = getQuestionsFromPool(category, family)
        
        if (poolQuestions.size >= questionsPerFamily) {
            log.info { "Pool hit for family ${family.id}" }
            cacheQuestions(family.id, context.hashCode(), poolQuestions.take(questionsPerFamily))
            return poolQuestions.take(questionsPerFamily)
        }
        
        // Level 3: AI 생성 (AssistantService가 있는 경우만)
        val generatedQuestions = assistantService?.let { service ->
            log.info { "Generating new questions for family ${family.id}" }
            service.generateQuestions(context)
        } ?: emptyList()
        
        if (generatedQuestions.isEmpty()) {
            log.warn { "No AI service available and pool exhausted for family ${family.id}" }
            return poolQuestions // 풀에서 가져온 것이라도 반환
        }
        
        val savedQuestionList = generatedQuestions
            .map { content ->
                Question(
                    content = content,
                    category = determineCategory(content),
                    isAIGenerated = true,
                    generatedDate = LocalDate.now()
                )
            }
        
        // 생성된 질문 저장
        val savedQuestions = questionRepository.saveAll(savedQuestionList)
        
        // 캐시에 저장
        cacheQuestions(family.id, context.hashCode(), savedQuestions)
        
        return savedQuestions
    }
    
    private fun getCachedQuestions(familyId: Long, contextHash: Int): List<Question>? {
        return redisTemplate?.let { redis ->
            val key = "question:family:$familyId:$contextHash"
            
            @Suppress("UNCHECKED_CAST")
            redis.opsForValue().get(key) as? List<Question>
        }
    }
    
    private fun cacheQuestions(familyId: Long, contextHash: Int, questions: List<Question>) {
        redisTemplate?.let { redis ->
            val key = "question:family:$familyId:$contextHash"
            redis.opsForValue().set(key, questions, Duration.ofHours(2))
        }
    }
    
    private fun getQuestionsFromPool(
        category: QuestionCategory,
        family: Family
    ): List<Question> {
        // 최근 30일 내 사용된 질문 제외
        val recentQuestionIds = questionRepository
            .findRecentlyUsedByFamily(family.id!!, 30)
            .map { it.id }
            .toSet()
        
        // 카테고리별로 사용 빈도가 낮은 질문 선택
        return questionRepository.findByCategoryAndIsAIGeneratedOrderByUsageCountAscCreatedAtDesc(category, true)
            .filterNot { recentQuestionIds.contains(it.id) }
            .take(questionsPerFamily)
    }
    
    private fun determineCategory(content: String): QuestionCategory {
        return when {
            content.contains(Regex("추억|예전|과거|어렸|옛날")) -> QuestionCategory.MEMORY
            content.contains(Regex("오늘|요즘|최근|지금")) -> QuestionCategory.DAILY
            content.contains(Regex("앞으로|미래|계획|하고 싶|되고 싶")) -> QuestionCategory.FUTURE
            content.contains(Regex("감사|고마|사랑")) -> QuestionCategory.GRATITUDE
            else -> QuestionCategory.GENERAL
        }
    }
    
    /**
     * 대량 질문 생성 (배치 처리용)
     */
    @Transactional
    suspend fun generateQuestionPool(category: QuestionCategory, count: Int = 20) {
        log.info { "Generating $count questions for category $category" }
        
        try {
            val prompts = listOf(
                "일반적인 ${category.korean} 관련 질문",
                "젊은 가족을 위한 ${category.korean} 질문",
                "3대가 함께하는 가족을 위한 ${category.korean} 질문"
            )
            
            prompts.forEach { prompt ->
                // 간단한 컨텍스트로 질문 생성
                val questions = mutableListOf<Question>()
                // 실제로는 bulk generation API를 사용해야 함
                // 여기서는 단순화를 위해 개별 생성
                repeat(count / prompts.size) {
                    delay(1000) // Rate limiting
                    val generated = assistantService?.generateQuestions(
                        com.challkathon.momento.domain.question.dto.MinimalFamilyContext(
                            familyId = 0,
                            memberCount = 4,
                            activityLevel = com.challkathon.momento.domain.question.entity.enums.ActivityLevel.MEDIUM,
                            preferredCategory = category
                        )
                    ) ?: emptyList()
                    questions.addAll(generated.map { content ->
                        Question(
                            content = content,
                            category = category,
                            isAIGenerated = true,
                            generatedDate = LocalDate.now()
                        )
                    })
                }
                
                questionRepository.saveAll(questions)
                log.info { "Saved ${questions.size} questions for prompt: $prompt" }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to generate question pool for category $category" }
        }
    }
}

// Kotlin coroutine의 delay를 import
private suspend fun delay(millis: Long) = kotlinx.coroutines.delay(millis)
