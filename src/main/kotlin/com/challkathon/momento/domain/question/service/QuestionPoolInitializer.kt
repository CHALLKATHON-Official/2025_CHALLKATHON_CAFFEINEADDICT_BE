package com.challkathon.momento.domain.question.service

import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

/**
 * 애플리케이션 시작 시 질문 풀을 초기화하는 컴포넌트
 * 첫 질문 생성 시 지연 시간을 제거하기 위해 사전에 질문 풀을 채워둠
 */
@Component
class QuestionPoolInitializer(
    private val questionPoolService: QuestionPoolService
) {
    
    private val logger = KotlinLogging.logger {}
    
    /**
     * 애플리케이션이 완전히 시작된 후 질문 풀 초기화
     * ApplicationReadyEvent를 사용하여 모든 Bean이 준비된 후 실행
     */
    @EventListener(ApplicationReadyEvent::class)
    fun initializeQuestionPoolOnStartup() {
        logger.info { "🚀 애플리케이션 시작 완료 - 질문 풀 초기화 시작" }
        
        try {
            val startTime = System.currentTimeMillis()
            
            // 질문 풀 초기화 (AI 생성 실패 시 폴백 질문 사용)
            questionPoolService.initializePool()
            
            val elapsedTime = System.currentTimeMillis() - startTime
            logger.info { "✅ 질문 풀 초기화 완료 (소요시간: ${elapsedTime}ms)" }
            logger.info { "이제 첫 질문 생성 요청도 즉시 응답됩니다!" }
            
        } catch (e: Exception) {
            logger.error(e) { "❌ 질문 풀 초기화 실패 - 폴백 질문으로 초기화 시도" }
            
            try {
                // AI 생성 실패 시 폴백 질문으로라도 초기화
                questionPoolService.initializeWithFallbackQuestions()
                logger.info { "✅ 폴백 질문으로 초기화 완료" }
            } catch (fe: Exception) {
                logger.error(fe) { "❌ 폴백 질문 초기화도 실패" }
            }
        }
    }
    
    /**
     * Bean 생성 시 로그 출력
     */
    @PostConstruct
    fun init() {
        logger.info { "QuestionPoolInitializer Bean 생성 완료 - 애플리케이션 시작 대기 중..." }
    }
}
