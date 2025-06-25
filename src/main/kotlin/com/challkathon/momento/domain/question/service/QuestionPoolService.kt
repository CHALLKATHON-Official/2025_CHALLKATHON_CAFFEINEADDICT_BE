package com.challkathon.momento.domain.question.service

import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import mu.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class QuestionPoolService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val questionGeneratorService: QuestionGeneratorService
) {
    
    private val logger = KotlinLogging.logger {}
    
    companion object {
        const val QUESTION_POOL_KEY = "question:pool"
        const val POOL_SIZE_PER_CATEGORY = 20
        const val MIN_POOL_SIZE = 10
    }
    
    /**
     * 캐시된 질문을 가져오거나, 없으면 새로 생성
     */
    fun getOrGenerateQuestion(userId: Long, category: QuestionCategory? = null): String {
        // 1. 먼저 캐시에서 질문 찾기
        val cachedQuestion = getQuestionFromPool(category)
        if (cachedQuestion != null) {
            logger.info { "캐시에서 질문 반환: $cachedQuestion" }
            return cachedQuestion
        }
        
        // 2. 캐시에 없으면 기본 질문 제공하고 비동기로 풀 채우기
        fillPoolAsync()
        
        // 3. 즉시 반환할 기본 질문
        return getDefaultQuestion(category)
    }
    
    /**
     * 질문 풀에서 질문 가져오기
     */
    private fun getQuestionFromPool(category: QuestionCategory?): String? {
        val key = if (category != null) {
            "$QUESTION_POOL_KEY:${category.name}"
        } else {
            "$QUESTION_POOL_KEY:GENERAL"
        }
        
        return try {
            val questions = redisTemplate.opsForList().range(key, 0, -1)
            if (!questions.isNullOrEmpty()) {
                // 랜덤하게 하나 선택하고 리스트에서 제거
                val randomIndex = (0 until questions.size).random()
                redisTemplate.opsForList().remove(key, 1, questions[randomIndex])
                questions[randomIndex] as? String
            } else {
                null
            }
        } catch (e: Exception) {
            logger.error(e) { "Redis에서 질문 가져오기 실패" }
            null
        }
    }
    
    /**
     * 카테고리별 기본 질문 제공
     */
    private fun getDefaultQuestion(category: QuestionCategory?): String {
        val defaultQuestions = mapOf(
            QuestionCategory.DAILY to listOf(
                "오늘 하루는 어떠셨나요?",
                "오늘 가장 기억에 남는 순간은 무엇인가요?",
                "오늘 감사했던 일이 있나요?"
            ),
            QuestionCategory.MEMORY to listOf(
                "가족과의 소중한 추억이 있나요?",
                "어린 시절 가장 행복했던 기억은 무엇인가요?",
                "다시 돌아가고 싶은 순간이 있나요?"
            ),
            QuestionCategory.FUTURE to listOf(
                "올해 이루고 싶은 목표가 있나요?",
                "가족과 함께 하고 싶은 일이 있나요?",
                "앞으로의 계획을 들려주세요."
            ),
            QuestionCategory.GRATITUDE to listOf(
                "가족에게 전하고 싶은 감사의 마음이 있나요?",
                "오늘 누군가에게 고마움을 느꼈나요?",
                "감사하게 생각하는 일상의 작은 것들이 있나요?"
            )
        )
        
        val questions = defaultQuestions[category] ?: defaultQuestions[QuestionCategory.DAILY]!!
        return questions.random()
    }
    
    /**
     * 비동기로 질문 풀 채우기
     */
    @Async
    fun fillPoolAsync() {
        logger.info { "비동기로 질문 풀 채우기 시작" }
        try {
            QuestionCategory.values().forEach { category ->
                val key = "$QUESTION_POOL_KEY:${category.name}"
                val currentSize = redisTemplate.opsForList().size(key) ?: 0
                
                if (currentSize < MIN_POOL_SIZE) {
                    logger.info { "$category 카테고리 질문 풀 채우기: 현재 $currentSize 개" }
                    // 실제로는 OpenAI API를 호출하지만, 여기서는 예시 질문 생성
                    generateQuestionsForPool(category, POOL_SIZE_PER_CATEGORY - currentSize.toInt())
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "질문 풀 채우기 실패" }
        }
    }
    
    /**
     * 스케줄러로 주기적으로 질문 풀 관리
     */
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    fun refillQuestionPool() {
        logger.info { "스케줄러: 질문 풀 재충전 시작" }
        QuestionCategory.values().forEach { category ->
            val key = "$QUESTION_POOL_KEY:${category.name}"
            val currentSize = redisTemplate.opsForList().size(key) ?: 0
            
            if (currentSize < POOL_SIZE_PER_CATEGORY) {
                generateQuestionsForPool(category, POOL_SIZE_PER_CATEGORY - currentSize.toInt())
            }
        }
    }
    
    /**
     * 질문 풀에 질문 생성 및 저장
     */
    private fun generateQuestionsForPool(category: QuestionCategory, count: Int) {
        try {
            // QuestionGeneratorService를 사용하여 AI로 질문 생성
            val generatedQuestions = questionGeneratorService.generateQuestionsForCategory(category, count)
            
            val key = "$QUESTION_POOL_KEY:${category.name}"
            
            generatedQuestions.forEach { question ->
                redisTemplate.opsForList().rightPush(key, question)
            }
            
            // TTL 설정 (7일)
            redisTemplate.expire(key, 7, TimeUnit.DAYS)
            
            logger.info { "${category.name} 카테고리에 ${generatedQuestions.size}개 질문 추가 완료 (AI 생성)" }
            
        } catch (e: Exception) {
            logger.error(e) { "AI 질문 생성 실패, 폴백 질문 사용" }
            // 실패 시 기본 질문 사용
            generateFallbackQuestions(category, count)
        }
    }
    
    /**
     * AI 생성 실패 시 폴백 질문 저장
     */
    private fun generateFallbackQuestions(category: QuestionCategory, count: Int) {
        val sampleQuestions = when (category) {
            QuestionCategory.DAILY -> listOf(
                "오늘 가장 행복했던 순간은 언제였나요?",
                "오늘 새롭게 배운 것이 있나요?",
                "오늘 만난 사람 중 가장 인상 깊었던 사람은 누구인가요?",
                "오늘 하루를 한 단어로 표현한다면?",
                "오늘 가장 맛있게 먹은 음식은 무엇인가요?"
            )
            QuestionCategory.MEMORY -> listOf(
                "가족과 함께한 최고의 여행지는 어디인가요?",
                "어린 시절 가장 좋아했던 놀이는 무엇인가요?",
                "처음으로 요리해본 음식은 무엇인가요?",
                "학창시절 가장 기억에 남는 선생님은 누구인가요?",
                "가장 오래된 친구와의 추억은 무엇인가요?"
            )
            QuestionCategory.FUTURE -> listOf(
                "올해 꼭 가보고 싶은 곳은 어디인가요?",
                "배우고 싶은 새로운 취미가 있나요?",
                "5년 후 나의 모습을 상상해본다면?",
                "가족과 함께 도전해보고 싶은 것이 있나요?",
                "은퇴 후 하고 싶은 일이 있나요?"
            )
            QuestionCategory.GRATITUDE -> listOf(
                "오늘 가장 감사한 일은 무엇인가요?",
                "최근에 받은 도움 중 가장 고마웠던 것은?",
                "가족 중 누구에게 가장 감사하나요?",
                "일상에서 당연하게 여기지만 감사한 것은?",
                "올해 가장 감사한 변화는 무엇인가요?"
            )
            QuestionCategory.GENERAL -> listOf(
                "요즘 가장 관심있는 것은 무엇인가요?",
                "최근에 읽은 책이나 본 영화가 있나요?",
                "요즘 즐겨 듣는 음악은 무엇인가요?",
                "주말에 주로 무엇을 하며 시간을 보내나요?",
                "최근에 시작한 새로운 습관이 있나요?"
            )
        }
        
        val key = "$QUESTION_POOL_KEY:${category.name}"
        val questionsToAdd = sampleQuestions.shuffled().take(count)
        
        questionsToAdd.forEach { question ->
            redisTemplate.opsForList().rightPush(key, question)
        }
        
        // TTL 설정 (7일)
        redisTemplate.expire(key, 7, TimeUnit.DAYS)
        
        logger.info { "${category.name} 카테고리에 ${questionsToAdd.size}개 폴백 질문 추가 완료" }
    }
}
