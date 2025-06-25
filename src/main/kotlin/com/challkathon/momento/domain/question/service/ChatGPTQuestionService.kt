package com.challkathon.momento.domain.question.service

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.challkathon.momento.domain.question.dto.response.GeneratedQuestionResponse
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.domain.question.entity.mapping.UserQuestion
import com.challkathon.momento.domain.question.repository.QuestionRepository
import com.challkathon.momento.domain.question.repository.UserQuestionRepository
import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.domain.user.repository.UserRepository
import com.challkathon.momento.domain.user.exception.UserException
import com.challkathon.momento.domain.user.exception.code.UserErrorStatus
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ChatGPTQuestionService(
    @Value("\${openai.api-key}")
    private val apiKey: String,
    
    @Value("\${openai.model:gpt-4o-mini}")
    private val model: String,
    
    @Value("\${openai.max-tokens:300}")
    private val maxTokens: Int,
    
    @Value("\${openai.temperature:0.8}")
    private val temperature: Double,
    
    private val answerHistoryService: AnswerHistoryService,
    private val questionRepository: QuestionRepository,
    private val userQuestionRepository: UserQuestionRepository,
    private val userRepository: UserRepository,
    private val questionPoolService: QuestionPoolService
) {
    
    private val logger = KotlinLogging.logger {}
    private val openAI = OpenAI(apiKey)
    
    @Transactional
    fun generatePersonalizedQuestion(userId: Long): GeneratedQuestionResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { UserException(UserErrorStatus.USER_NOT_FOUND) }
        
        return generatePersonalizedQuestion(user)
    }
    
    @Transactional
    fun generatePersonalizedQuestion(user: User): GeneratedQuestionResponse {
        return try {
            logger.info { "사용자 ${user.id}를 위한 맞춤형 질문 생성 시작" }
            
            // 최근 24시간 내 생성된 질문 확인
            val recentQuestions = userQuestionRepository.findRecentQuestionsByUser(
                user = user,
                startTime = LocalDateTime.now().minusHours(24)
            )
            
            if (recentQuestions.size >= 5) {
                logger.warn { "사용자 ${user.id}가 24시간 내 질문 생성 한도 초과" }
                throw IllegalStateException("하루 질문 생성 한도를 초과했습니다. (최대 5개)")
            }
            
            // 1. 먼저 캐시에서 질문 가져오기 시도 (빠른 응답)
            val startTime = System.currentTimeMillis()
            val cachedQuestion = questionPoolService.getOrGenerateQuestion(user.id)
            val cacheTime = System.currentTimeMillis() - startTime
            
            if (cacheTime < 100) {
                logger.info { "캐시에서 질문 반환 (${cacheTime}ms)" }
                return saveQuestionAndReturn(cachedQuestion, user, isFromCache = true)
            }
            
            // 2. 캐시에 없으면 AI 생성 (기존 로직)
            val personalContext = answerHistoryService.generatePersonalizedContext(user)
            val userPrompt = if (personalContext.isNotEmpty()) {
                personalContext
            } else {
                "이 사용자에게 맞는 따뜻한 가족 질문 1개를 생성해주세요."
            }
            
            val response = runBlocking {
                openAI.chatCompletion(
                    ChatCompletionRequest(
                        model = ModelId(model),
                        messages = listOf(
                            ChatMessage(
                                role = ChatRole.System,
                                content = getPersonalizedSystemPrompt()
                            ),
                            ChatMessage(
                                role = ChatRole.User,
                                content = userPrompt
                            )
                        ),
                        maxTokens = maxTokens,
                        temperature = temperature
                    )
                )
            }
            
            val content = response.choices.first().message.content ?: ""
            val questionContent = parsePersonalizedQuestion(content)
            
            // 중복 질문 확인
            val isDuplicate = userQuestionRepository.existsSimilarQuestionRecently(
                user = user,
                content = questionContent,
                startTime = LocalDateTime.now().minusDays(7)
            )
            
            if (isDuplicate) {
                logger.info { "중복 질문 감지, 재생성 시도" }
                return generatePersonalizedQuestion(user) // 재귀 호출로 새 질문 생성
            }
            
            // 카테고리 분류
            val category = classifyQuestionCategory(questionContent)
            
            // Question 엔티티 생성 및 저장
            val question = Question(
                content = questionContent,
                category = category,
                isAIGenerated = true,
                generatedDate = LocalDate.now()
            )
            val savedQuestion = questionRepository.save(question)
            
            // UserQuestion 매핑 생성 및 저장
            val userQuestion = UserQuestion(
                user = user,
                question = savedQuestion,
                aiModel = model,
                isAnswered = false
            )
            userQuestionRepository.save(userQuestion)
            
            logger.info { "사용자 ${user.id}를 위한 맞춤형 질문 생성 및 저장 완료: ${savedQuestion.id}" }
            
            return GeneratedQuestionResponse.from(savedQuestion)
            
        } catch (e: IllegalStateException) {
            throw e
        } catch (e: Exception) {
            logger.error(e) { "맞춤형 질문 생성 실패" }
            // 폴백: 미리 정의된 질문 사용
            val fallbackQuestion = createFallbackQuestion()
            return GeneratedQuestionResponse.from(fallbackQuestion)
        }
    }
    
    private fun getPersonalizedSystemPrompt(): String {
        return """
            # 역할
            당신은 한국 가족들의 소통을 돕는 맞춤형 질문 생성 전문가입니다.
            
            # 기본 규칙
            1. 질문 길이: 10-50자
            2. 질문 개수: 정확히 1개만
            3. 언어: 한국어, 존댓말
            4. 톤: 따뜻하고 개인적
            5. 개인화: 사용자의 답변 이력을 반영
            
            # 개인화 원칙
            - 사용자의 이전 답변에서 나타난 관심사나 성향을 반영
            - 답변 패턴을 분석하여 더 깊이 있는 질문 생성
            - 가족 관계 개선에 도움이 되는 방향으로 질문
            
            # 출력 형식
            질문 1개만 생성 (번호나 불필요한 텍스트 없이)
            
            # 금지 사항
            - 민감한 주제 (정치, 종교, 돈)
            - 부정적이거나 갈등 유발 질문
            - 이전 답변과 완전히 동일한 질문
        """.trimIndent()
    }
    
    private fun parsePersonalizedQuestion(content: String): String {
        val question = content.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.contains("?") }
            .firstOrNull()
            
        return question ?: getPersonalizedFallbackQuestion()
    }
    
    private fun getPersonalizedFallbackQuestion(): String {
        return "오늘 하루 중 가장 소중했던 순간은 언제인가요?"
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
    
    @Transactional
    private fun saveQuestionAndReturn(
        questionContent: String,
        user: User,
        isFromCache: Boolean = false
    ): GeneratedQuestionResponse {
        // 카테고리 분류
        val category = classifyQuestionCategory(questionContent)
        
        // Question 엔티티 생성 및 저장
        val question = Question(
            content = questionContent,
            category = category,
            isAIGenerated = !isFromCache,
            generatedDate = LocalDate.now()
        )
        val savedQuestion = questionRepository.save(question)
        
        // UserQuestion 매핑 생성 및 저장
        val userQuestion = UserQuestion(
            user = user,
            question = savedQuestion,
            aiModel = if (isFromCache) "cached" else model,
            isAnswered = false
        )
        userQuestionRepository.save(userQuestion)
        
        logger.info { "사용자 ${user.id}를 위한 질문 저장 완료: ${savedQuestion.id} (캐시: $isFromCache)" }
        
        return GeneratedQuestionResponse.from(savedQuestion)
    }
}
