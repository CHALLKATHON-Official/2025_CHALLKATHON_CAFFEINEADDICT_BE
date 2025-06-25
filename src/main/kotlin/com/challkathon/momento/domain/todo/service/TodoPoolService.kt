package com.challkathon.momento.domain.todo.service

import com.challkathon.momento.domain.todo.entity.enums.TodoCategory
import mu.KotlinLogging
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class TodoPoolService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val todoGeneratorService: TodoGeneratorService
) {
    
    private val logger = KotlinLogging.logger {}
    
    companion object {
        const val TODO_POOL_KEY = "todo:pool"
        const val POOL_SIZE_PER_CATEGORY = 30  // 카테고리별 30개
        const val MIN_POOL_SIZE = 10  // 최소 10개 유지
        const val REFILL_THRESHOLD = 0.3  // 30% 이하일 때 보충
    }
    
    /**
     * 애플리케이션 시작 시 Todo 풀 초기화
     */
    fun initializePool() {
        logger.info { "Todo 풀 초기화 시작" }
        
        TodoCategory.values().forEach { category ->
            val key = "$TODO_POOL_KEY:${category.name}"
            val currentSize = redisTemplate.opsForList().size(key) ?: 0
            
            if (currentSize < POOL_SIZE_PER_CATEGORY) {
                logger.info { "$category 카테고리 초기화: 현재 ${currentSize}개, 목표 ${POOL_SIZE_PER_CATEGORY}개" }
                generateTodosForPool(category, POOL_SIZE_PER_CATEGORY - currentSize.toInt())
            }
        }
    }
    
    /**
     * 폴백 Todo로 초기화 (실패 시)
     */
    fun initializeWithFallbackTodos() {
        logger.info { "AI 생성 실패 - 폴백 Todo로 초기화" }
        
        TodoCategory.values().forEach { category ->
            generateFallbackTodos(category, POOL_SIZE_PER_CATEGORY)
        }
    }
    
    /**
     * 풀에서 Todo 하나 가져오기
     */
    fun getRandomTodoFromPool(category: TodoCategory): String? {
        val key = "$TODO_POOL_KEY:${category.name}"
        
        return try {
            val todo = redisTemplate.opsForList().rightPop(key) as? String
            
            if (todo != null) {
                val currentSize = redisTemplate.opsForList().size(key) ?: 0
                logger.debug { "$category 풀에서 Todo 추출: $todo (남은 개수: $currentSize)" }
                
                // 풀 크기가 임계값 이하로 떨어지면 비동기로 보충
                if (currentSize < MIN_POOL_SIZE) {
                    logger.info { "$category 풀 크기 부족 (${currentSize}개), 비동기 보충 시작" }
                    refillPoolAsync(category)
                }
            }
            
            todo
        } catch (e: Exception) {
            logger.error { "풀에서 Todo 추출 실패: ${e.message}" }
            null
        }
    }
    
    /**
     * 여러 개의 Todo를 풀에서 가져오기
     */
    fun getRandomTodosFromPool(category: TodoCategory, count: Int): List<String> {
        val todos = mutableListOf<String>()
        repeat(count) {
            getRandomTodoFromPool(category)?.let { todos.add(it) }
        }
        return todos
    }
    
    /**
     * 혼합 카테고리로 Todo 가져오기
     */
    fun getMixedTodosFromPool(count: Int): List<String> {
        val todos = mutableListOf<String>()
        val categories = TodoCategory.values()
        
        repeat(count) { index ->
            val category = categories[index % categories.size]
            getRandomTodoFromPool(category)?.let { todos.add(it) }
        }
        
        return todos
    }
    
    /**
     * 풀에 새로운 Todo 추가
     */
    fun addTodoToPool(category: TodoCategory, todoContent: String) {
        val key = "$TODO_POOL_KEY:${category.name}"
        
        try {
            redisTemplate.opsForList().leftPush(key, todoContent)
            redisTemplate.expire(key, 24, TimeUnit.HOURS)
            logger.debug { "$category 풀에 Todo 추가: $todoContent" }
        } catch (e: Exception) {
            logger.error { "풀에 Todo 추가 실패: ${e.message}" }
        }
    }
    
    /**
     * 비동기로 풀 보충
     */
    @Async
    fun refillPoolAsync(category: TodoCategory) {
        val targetCount = POOL_SIZE_PER_CATEGORY - MIN_POOL_SIZE
        generateTodosForPool(category, targetCount)
    }
    
    /**
     * 스케줄러로 풀 상태 모니터링 및 보충
     */
    @Scheduled(fixedRate = 1800000) // 30분마다
    fun monitorAndRefillPools() {
        logger.debug { "Todo 풀 상태 모니터링 시작" }
        
        TodoCategory.values().forEach { category ->
            val key = "$TODO_POOL_KEY:${category.name}"
            val currentSize = redisTemplate.opsForList().size(key) ?: 0
            val threshold = (POOL_SIZE_PER_CATEGORY * REFILL_THRESHOLD).toInt()
            
            if (currentSize < threshold) {
                logger.info { "$category 풀 보충 필요: ${currentSize}개 (임계값: ${threshold}개)" }
                val refillCount = POOL_SIZE_PER_CATEGORY - currentSize.toInt()
                generateTodosForPool(category, refillCount)
            }
        }
    }
    
    /**
     * 풀 상태 조회
     */
    fun getPoolStatus(): Map<String, Long> {
        return TodoCategory.values().associate { category ->
            val key = "$TODO_POOL_KEY:${category.name}"
            category.name to (redisTemplate.opsForList().size(key) ?: 0)
        }
    }
    
    /**
     * AI를 통해 풀용 Todo 생성
     */
    private fun generateTodosForPool(category: TodoCategory, count: Int) {
        try {
            val generatedTodos = todoGeneratorService.generateBucketListTodos(category, count)
            
            generatedTodos.forEach { todoContent ->
                addTodoToPool(category, todoContent)
            }
            
            logger.info { "$category 카테고리에 ${generatedTodos.size}개 Todo 생성 완료" }
        } catch (e: Exception) {
            logger.error { "$category Todo 생성 실패, 폴백으로 대체: ${e.message}" }
            generateFallbackTodos(category, count)
        }
    }
    
    /**
     * 폴백 Todo 생성
     */
    private fun generateFallbackTodos(category: TodoCategory, count: Int) {
        val fallbackTodos = when (category) {
            TodoCategory.TRAVEL -> listOf(
                "가족과 함께 국내 여행지 방문하기",
                "가족 모두가 가고 싶어하는 해외 여행 계획하기",
                "근처 맛집 탐방하며 가족 시간 보내기"
            )
            TodoCategory.ACTIVITY -> listOf(
                "가족과 함께 등산하기",
                "가족 스포츠 활동 참여하기",
                "집에서 가족 게임 대회 열기"
            )
            TodoCategory.EXPERIENCE -> listOf(
                "가족과 함께 새로운 요리 배우기",
                "가족 모두가 처음 해보는 활동 도전하기",
                "가족 전통 만들기"
            )
            TodoCategory.HOBBY -> listOf(
                "가족이 함께 할 수 있는 취미 찾기",
                "가족 사진 앨범 만들기",
                "가족 정원 가꾸기"
            )
            TodoCategory.LEARNING -> listOf(
                "가족이 함께 새로운 언어 배우기",
                "가족 독서 모임 만들기",
                "서로에게 특기 가르쳐주기"
            )
            TodoCategory.BONDING -> listOf(
                "가족 회의 시간 정기적으로 갖기",
                "가족 각자의 꿈 공유하기",
                "감사 인사 주고받기"
            )
        }
        
        repeat(count) { index ->
            val todo = fallbackTodos[index % fallbackTodos.size]
            addTodoToPool(category, todo)
        }
        
        logger.info { "$category 카테고리에 ${count}개 폴백 Todo 추가 완료" }
    }
}