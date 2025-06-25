package com.challkathon.momento.domain.question.service

import com.challkathon.momento.domain.question.entity.Answer
import com.challkathon.momento.domain.question.repository.AnswerRepository
import com.challkathon.momento.domain.user.entity.User
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AnswerHistoryService(
    private val answerRepository: AnswerRepository
) {
    
    private val logger = KotlinLogging.logger {}
    
    fun getUserAnswerHistory(user: User, limit: Int = 10): List<Answer> {
        return answerRepository.findRecentAnswersByUser(user, limit)
    }
    
    fun generatePersonalizedContext(user: User): String {
        val recentAnswers = getUserAnswerHistory(user, 5)
        
        if (recentAnswers.isEmpty()) {
            logger.info { "사용자 ${user.id}의 답변 이력이 없어 기본 질문을 생성합니다" }
            return ""
        }
        
        val answerSummary = recentAnswers.joinToString("\n") { answer ->
            "질문: ${answer.familyQuestion.question.content}\n답변: ${answer.content}"
        }
        
        logger.info { "사용자 ${user.id}의 최근 답변 ${recentAnswers.size}개를 기반으로 맞춤형 질문을 생성합니다" }
        
        return """
        이 사용자의 최근 답변 내용:
        $answerSummary
        
        위 답변들을 분석해서 이 사용자의 관심사, 성향, 가족 관계를 파악하고 
        그에 맞는 개인화된 질문 1개를 생성해주세요.
        """.trimIndent()
    }
}