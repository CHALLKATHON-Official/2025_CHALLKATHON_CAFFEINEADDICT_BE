package com.challkathon.momento.domain.question.service

import com.challkathon.momento.domain.question.dto.response.GeneratedQuestionResponse
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import com.challkathon.momento.domain.question.entity.mapping.FamilyQuestion
import com.challkathon.momento.domain.question.repository.QuestionRepository
import com.challkathon.momento.domain.question.repository.FamilyQuestionRepository
import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.repository.UserRepository
import com.challkathon.momento.domain.user.exception.UserException
import com.challkathon.momento.domain.user.exception.code.UserErrorStatus
import com.challkathon.momento.domain.family.exception.FamilyException
import com.challkathon.momento.domain.family.exception.code.FamilyErrorStatus
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 가족 기반 질문 생성 서비스
 * Family -> FamilyQuestion 구조로 질문 생성 및 관리
 */
@Service
@Transactional(readOnly = true)
class ChatGPTQuestionService(
    private val answerHistoryService: AnswerHistoryService,
    private val questionRepository: QuestionRepository,
    private val familyQuestionRepository: FamilyQuestionRepository,
    private val userRepository: UserRepository,
    private val questionPoolService: QuestionPoolService
) {
    
    private val logger = KotlinLogging.logger {}
    
    @Transactional
    fun generatePersonalizedQuestion(userId: Long): GeneratedQuestionResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }
        
        return generatePersonalizedQuestion(user)
    }
    
    @Transactional
    fun generatePersonalizedQuestion(user: User): GeneratedQuestionResponse {
        return try {
            logger.info { "🎯 사용자 ${user.id}를 위한 질문 생성 요청 시작" }
            
            // 가족 소속 확인 - 가족이 없으면 질문 생성 불가
            val family = user.family
                ?: throw FamilyException(FamilyErrorStatus.FAMILY_DID_NOT_SET)
            
            logger.info { "👨‍👩‍👧‍👦 사용자 ${user.id}의 가족 ID: ${family.id}" }
            
            // 최근 24시간 내 가족에게 할당된 질문 확인 (일일 한도 체크)
            val recentFamilyQuestions = familyQuestionRepository.findByFamilyIdAndDateRange(
                familyId = family.id!!,
                startDate = LocalDateTime.now().minusHours(24),
                endDate = LocalDateTime.now()
            )
            
            if (recentFamilyQuestions.size >= 5) {
                logger.warn { "가족 ${family.id}이 24시간 내 질문 생성 한도 초과" }
                throw IllegalStateException("하루 질문 생성 한도를 초과했습니다. (최대 5개)")
            }
            
            // 개인화: 사용자의 답변 히스토리를 기반으로 카테고리 선택
            val preferredCategory = selectCategoryBasedOnHistory(user)
            
            // 항상 캐시에서 질문 가져오기 (즉시 응답)
            val startTime = System.currentTimeMillis()
            val questionContent = questionPoolService.getQuestionFromCache(user.id!!, preferredCategory)
            val responseTime = System.currentTimeMillis() - startTime
            
            logger.info { "✅ 캐시에서 질문 가져옴 (${responseTime}ms, 카테고리: $preferredCategory): $questionContent" }
            
            // 중복 질문 확인 (최근 7일간 가족에게 할당된 질문과 비교)
            var finalQuestion = questionContent
            var attempts = 0
            val maxAttempts = 5
            
            while (attempts < maxAttempts) {
                val isDuplicate = checkDuplicateFamilyQuestion(family.id!!, finalQuestion)
                
                if (!isDuplicate) {
                    break
                }
                
                logger.info { "중복 질문 감지, 다른 질문 선택 (${attempts + 1}/${maxAttempts})" }
                finalQuestion = questionPoolService.getQuestionFromCache(user.id!!, preferredCategory)
                attempts++
            }
            
            // 카테고리 분류
            val category = classifyQuestionCategory(finalQuestion)
            
            // Question 엔티티 생성 및 저장
            val question = Question(
                content = finalQuestion,
                category = category,
                isAIGenerated = false, // 캐시에서 가져왔으므로 false
                generatedDate = LocalDate.now()
            )
            val savedQuestion = questionRepository.save(question)
            
            // FamilyQuestion 매핑 생성 및 저장
            val familyQuestion = FamilyQuestion(
                question = savedQuestion,
                family = family,
                assignedAt = LocalDateTime.now(),
                dueDate = LocalDateTime.now().plusDays(1), // 1일 후 만료
                status = FamilyQuestionStatus.ASSIGNED
            )
            familyQuestionRepository.save(familyQuestion)
            
            logger.info { "💾 가족 ${family.id}에게 질문 할당 완료: ${savedQuestion.id} (총 응답시간: ${responseTime}ms)" }
            
            return GeneratedQuestionResponse.from(savedQuestion)
            
        } catch (e: IllegalStateException) {
            logger.warn { "⚠️ 사용자 ${user.id} 질문 생성 제한: ${e.message}" }
            throw e
        } catch (e: FamilyException) {
            logger.warn { "⚠️ 사용자 ${user.id} 가족 관련 오류: ${e.message}" }
            throw e
        } catch (e: Exception) {
            logger.error(e) { "❌ 질문 생성 실패 - 폴백 질문 사용" }
            // 폴백: 미리 정의된 질문 사용
            val fallbackQuestion = createFallbackQuestion()
            logger.info { "🔄 폴백 질문으로 응답: ${fallbackQuestion.content}" }
            return GeneratedQuestionResponse.from(fallbackQuestion)
        }
    }
    
    /**
     * 사용자의 답변 히스토리를 기반으로 카테고리 선택
     */
    private fun selectCategoryBasedOnHistory(user: User): QuestionCategory {
        return try {
            // 사용자의 최근 답변 패턴 분석
            val recentAnswers = answerHistoryService.getUserAnswerHistory(user, 10)
            
            if (recentAnswers.isEmpty()) {
                // 답변 히스토리가 없으면 랜덤 카테고리
                QuestionCategory.values().random()
            } else {
                // 간단한 개인화: 가장 적게 답변한 카테고리 선택
                val categoryCounts = recentAnswers
                    .groupBy { it.familyQuestion.question.category }
                    .mapValues { it.value.size }
                
                // 가장 적게 답변한 카테고리 찾기
                val leastAnsweredCategory = QuestionCategory.values()
                    .minByOrNull { categoryCounts[it] ?: 0 }
                    ?: QuestionCategory.GENERAL
                
                logger.debug { "사용자 ${user.id}의 선호 카테고리: $leastAnsweredCategory" }
                leastAnsweredCategory
            }
            
        } catch (e: Exception) {
            logger.warn(e) { "카테고리 선택 실패, 랜덤 카테고리 사용" }
            QuestionCategory.values().random()
        }
    }
    
    /**
     * 가족에게 최근 할당된 질문과 중복 여부 확인
     */
    private fun checkDuplicateFamilyQuestion(familyId: Long, content: String): Boolean {
        val recentQuestions = familyQuestionRepository.findByFamilyIdAndDateRange(
            familyId = familyId,
            startDate = LocalDateTime.now().minusDays(7),
            endDate = LocalDateTime.now()
        )
        
        return recentQuestions.any { 
            it.question.content.contains(content.take(20)) || 
            content.contains(it.question.content.take(20))
        }
    }
    
    private fun classifyQuestionCategory(content: String): QuestionCategory {
        return when {
            content.contains("추억") || content.contains("예전") || content.contains("기억") -> 
                QuestionCategory.MEMORY
            content.contains("오늘") || content.contains("최근") || content.contains("요즘") -> 
                QuestionCategory.DAILY
            content.contains("앞으로") || content.contains("미래") || content.contains("계획") -> 
                QuestionCategory.FUTURE
            content.contains("감사") || content.contains("고마") || content.contains("소중") -> 
                QuestionCategory.GRATITUDE
            else -> QuestionCategory.GENERAL
        }
    }
    
    @Transactional
    private fun createFallbackQuestion(): Question {
        val fallbackQuestions = listOf(
            "오늘 하루 중 가장 소중했던 순간은 언제인가요?" to QuestionCategory.DAILY,
            "가족과 함께 다시 가고 싶은 장소는 어디인가요?" to QuestionCategory.MEMORY,
            "올해 가족과 함께 이루고 싶은 목표가 있나요?" to QuestionCategory.FUTURE,
            "가족에게 전하고 싶은 감사의 마음이 있나요?" to QuestionCategory.GRATITUDE
        )
        
        val (content, category) = fallbackQuestions.random()
        
        val question = Question(
            content = content,
            category = category,
            isAIGenerated = false,
            generatedDate = LocalDate.now()
        )
        
        return questionRepository.save(question)
    }
}