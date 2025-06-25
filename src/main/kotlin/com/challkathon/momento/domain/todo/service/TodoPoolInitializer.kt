package com.challkathon.momento.domain.todo.service

import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class TodoPoolInitializer(
    private val todoPoolService: TodoPoolService
) {
    
    private val logger = KotlinLogging.logger {}
    
    @EventListener(ApplicationReadyEvent::class)
    @Async
    fun initializeTodoPool() {
        try {
            logger.info { "🚀 Todo 풀 초기화 시작" }
            val startTime = System.currentTimeMillis()
            
            todoPoolService.initializePool()
            
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            logger.info { "✅ Todo 풀 초기화 완료 (소요시간: ${duration}ms)" }
            
            // 풀 상태 로깅
            val poolStatus = todoPoolService.getPoolStatus()
            poolStatus.forEach { (category, count) ->
                logger.info { "📦 $category: ${count}개" }
            }
        } catch (e: Exception) {
            logger.error { "❌ Todo 풀 초기화 실패: ${e.message}" }
            logger.info { "🔄 폴백 Todo로 초기화 시도" }
            todoPoolService.initializeWithFallbackTodos()
        }
    }
}