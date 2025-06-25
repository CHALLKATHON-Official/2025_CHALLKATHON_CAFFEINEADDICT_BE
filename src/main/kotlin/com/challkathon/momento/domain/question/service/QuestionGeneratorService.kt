package com.challkathon.momento.domain.question.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * OpenAI API를 사용한 질문 생성 전용 서비스
 * ChatGPTQuestionService와 QuestionPoolService 간의 순환 참조를 방지하기 위해 분리
 */
@Service
class QuestionGeneratorService(
    @Value("\${openai.api-key}")
    private val apiKey: String,
    
    @Value("\${openai.model:gpt-4o-mini}")
    private val model: String,
    
    @Value("\${openai.max-tokens:300}")
    private val maxTokens: Int,
    
    @Value("\${openai.temperature:0.8}")
    private val temperature: Double
) {
    
    private val logger = KotlinLogging.logger {}
    private val openAI = OpenAI(apiKey)
    
    /**
     * 카테고리에 맞는 질문들을 생성
     */
    fun generateQuestionsForCategory(category: QuestionCategory, count: Int = 5): List<String> {
        return try {
            logger.info { "${category.name} 카테고리용 질문 $count 개 생성 시작" }
            
            val prompt = createPromptForCategory(category, count)
            
            val response = runBlocking {
                openAI.chatCompletion(
                    ChatCompletionRequest(
                        model = ModelId(model),
                        messages = listOf(
                            ChatMessage(
                                role = ChatRole.System,
                                content = getSystemPromptForBulkGeneration()
                            ),
                            ChatMessage(
                                role = ChatRole.User,
                                content = prompt
                            )
                        ),
                        maxTokens = maxTokens * count,
                        temperature = temperature
                    )
                )
            }
            
            val content = response.choices.first().message.content ?: ""
            parseMultipleQuestions(content, count)
            
        } catch (e: Exception) {
            logger.error(e) { "${category.name} 카테고리 질문 생성 실패" }
            getFallbackQuestions(category, count)
        }
    }
    
    /**
     * 개인화된 질문 1개 생성
     */
    fun generatePersonalizedQuestion(context: String): String {
        return try {
            val response = runBlocking {
                openAI.chatCompletion(
                    ChatCompletionRequest(
                        model = ModelId(model),
                        messages = listOf(
                            ChatMessage(
                                role = ChatRole.System,
                                content = getSystemPromptForPersonalized()
                            ),
                            ChatMessage(
                                role = ChatRole.User,
                                content = context
                            )
                        ),
                        maxTokens = maxTokens,
                        temperature = temperature
                    )
                )
            }
            
            val content = response.choices.first().message.content ?: ""
            parseQuestionContent(content)
            
        } catch (e: Exception) {
            logger.error(e) { "개인화 질문 생성 실패" }
            "오늘 하루 중 가장 소중했던 순간은 언제인가요?"
        }
    }
    
    private fun createPromptForCategory(category: QuestionCategory, count: Int): String {
        val categoryDescription = when (category) {
            QuestionCategory.DAILY -> "일상과 오늘 하루에 대한"
            QuestionCategory.MEMORY -> "과거의 추억과 기억에 대한"
            QuestionCategory.FUTURE -> "미래의 계획과 희망에 대한"
            QuestionCategory.GRATITUDE -> "감사와 고마움에 대한"
            QuestionCategory.GENERAL -> "일반적인 가족 소통을 위한"
        }
        
        return "$categoryDescription 따뜻한 가족 질문을 $count 개 생성해주세요."
    }
    
    private fun getSystemPromptForBulkGeneration(): String {
        return """
            # 역할
            당신은 한국 가족들의 소통을 돕는 질문 생성 전문가입니다.
            
            # 기본 규칙
            1. 질문 길이: 10-50자
            2. 언어: 한국어, 존댓말
            3. 톤: 따뜻하고 긍정적
            4. 난이도: 모든 연령대가 답변 가능
            5. 다양성: 각 질문은 서로 다른 관점에서 접근
            
            # 출력 형식
            - 각 질문은 새로운 줄에 작성
            - 번호나 불필요한 설명 없이 질문만 작성
            - 모든 질문은 물음표(?)로 끝남
            
            # 금지 사항
            - 민감한 주제 (정치, 종교, 돈)
            - 부정적이거나 갈등 유발 질문
            - 특정 구성원만 답할 수 있는 질문
            - 중복되거나 유사한 질문
        """.trimIndent()
    }
    
    private fun getSystemPromptForPersonalized(): String {
        return """
            # 역할
            당신은 한국 가족들의 소통을 돕는 맞춤형 질문 생성 전문가입니다.
            
            # 기본 규칙
            1. 질문 길이: 10-50자
            2. 질문 개수: 정확히 1개만
            3. 언어: 한국어, 존댓말
            4. 톤: 따뜻하고 개인적
            5. 개인화: 제공된 컨텍스트를 반영
            
            # 출력 형식
            질문 1개만 생성 (번호나 불필요한 텍스트 없이)
            
            # 금지 사항
            - 민감한 주제 (정치, 종교, 돈)
            - 부정적이거나 갈등 유발 질문
        """.trimIndent()
    }
    
    private fun parseMultipleQuestions(content: String, expectedCount: Int): List<String> {
        val questions = content.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.contains("?") }
            .take(expectedCount)
        
        return if (questions.isEmpty()) {
            getFallbackQuestions(QuestionCategory.GENERAL, expectedCount)
        } else {
            questions
        }
    }
    
    private fun parseQuestionContent(content: String): String {
        return content.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.contains("?") }
            .firstOrNull() ?: "오늘 하루 중 가장 소중했던 순간은 언제인가요?"
    }
    
    private fun getFallbackQuestions(category: QuestionCategory, count: Int): List<String> {
        val fallbacks = mapOf(
            QuestionCategory.DAILY to listOf(
                "오늘 하루는 어떠셨나요?",
                "오늘 가장 기억에 남는 순간은 무엇인가요?",
                "오늘 감사했던 일이 있나요?",
                "오늘 새롭게 배운 것이 있나요?",
                "오늘 만난 사람 중 인상 깊었던 사람은?"
            ),
            QuestionCategory.MEMORY to listOf(
                "가족과의 소중한 추억이 있나요?",
                "어린 시절 가장 행복했던 기억은?",
                "다시 돌아가고 싶은 순간이 있나요?",
                "가족과 함께한 최고의 여행지는?",
                "처음으로 요리해본 음식은?"
            ),
            QuestionCategory.FUTURE to listOf(
                "올해 이루고 싶은 목표가 있나요?",
                "가족과 함께 하고 싶은 일이 있나요?",
                "앞으로의 계획을 들려주세요.",
                "올해 꼭 가보고 싶은 곳은?",
                "배우고 싶은 새로운 취미가 있나요?"
            ),
            QuestionCategory.GRATITUDE to listOf(
                "가족에게 전하고 싶은 감사의 마음이 있나요?",
                "오늘 누군가에게 고마움을 느꼈나요?",
                "감사하게 생각하는 일상의 작은 것들이 있나요?",
                "최근에 받은 도움 중 가장 고마웠던 것은?",
                "올해 가장 감사한 변화는?"
            ),
            QuestionCategory.GENERAL to listOf(
                "요즘 가장 관심있는 것은 무엇인가요?",
                "최근에 읽은 책이나 본 영화가 있나요?",
                "요즘 즐겨 듣는 음악은?",
                "주말에 주로 무엇을 하며 시간을 보내나요?",
                "최근에 시작한 새로운 습관이 있나요?"
            )
        )
        
        return (fallbacks[category] ?: fallbacks[QuestionCategory.GENERAL]!!).take(count)
    }
}
