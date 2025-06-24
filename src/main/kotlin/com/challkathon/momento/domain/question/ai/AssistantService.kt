package com.challkathon.momento.domain.question.ai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.challkathon.momento.domain.question.config.OpenAIConfig
import com.challkathon.momento.domain.question.config.PromptConfig
import com.challkathon.momento.domain.question.dto.MinimalFamilyContext
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import jakarta.annotation.PostConstruct

private val log = KotlinLogging.logger {}

@Service
@ConditionalOnProperty(name = ["openai.enabled"], havingValue = "true", matchIfMissing = true)
class AssistantService(
    private val openAIConfig: OpenAIConfig,
    private val promptConfig: PromptConfig,
    @Value("\${openai.assistant.name:momento-family-question-v2}") 
    private val assistantName: String
) {
    private val openAI: OpenAI by lazy {
        try {
            OpenAI(token = openAIConfig.apiKey)
        } catch (e: Exception) {
            log.warn { "Failed to initialize OpenAI client: ${e.message}" }
            throw AIException("OpenAI 클라이언트 초기화 실패", e)
        }
    }

    @PostConstruct
    fun initialize() {
        log.info { "AssistantService initialized with model: ${openAIConfig.model}" }
    }

    suspend fun generateQuestions(context: MinimalFamilyContext): List<String> {
        try {
            val systemPrompt = promptConfig.systemInstructions
            val userPrompt = context.toPrompt()
            
            val chatRequest = ChatCompletionRequest(
                model = ModelId(openAIConfig.model),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = systemPrompt
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = userPrompt
                    )
                ),
                temperature = openAIConfig.temperature,
                maxTokens = 500
            )
            
            val completion = openAI.chatCompletion(chatRequest)
            val content = completion.choices.firstOrNull()?.message?.content
                ?: throw AIException("OpenAI로부터 응답을 받지 못했습니다")
            
            return parseQuestions(content)
            
        } catch (e: Exception) {
            log.error(e) { "Failed to generate questions for family ${context.familyId}" }
            throw AIException("질문 생성 실패: ${e.message}", e)
        }
    }

    /**
     * 비동기 질문 생성 (runBlocking 래퍼)
     */
    fun generateQuestionsSync(context: MinimalFamilyContext): List<String> = runBlocking {
        generateQuestions(context)
    }

    /**
     * 스트리밍 방식으로 질문 생성 (향후 확장용)
     */
    suspend fun generateQuestionsStream(context: MinimalFamilyContext): List<String> {
        try {
            val systemPrompt = promptConfig.systemInstructions
            val userPrompt = context.toPrompt()
            
            val chatRequest = ChatCompletionRequest(
                model = ModelId(openAIConfig.model),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = systemPrompt
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = userPrompt
                    )
                ),
                temperature = openAIConfig.temperature,
                maxTokens = 500
            )
            
            val responses = mutableListOf<String>()
            openAI.chatCompletions(chatRequest).collect { completion ->
                completion.choices.forEach { choice ->
                    choice.delta?.content?.let { content ->
                        responses.add(content)
                    }
                }
            }
            
            val fullContent = responses.joinToString("")
            return parseQuestions(fullContent)
            
        } catch (e: Exception) {
            log.error(e) { "Failed to generate questions via streaming for family ${context.familyId}" }
            throw AIException("스트리밍 질문 생성 실패: ${e.message}", e)
        }
    }

    /**
     * 응답을 개별 질문으로 파싱
     */
    private fun parseQuestions(content: String): List<String> {
        return content.split("\n")
            .map { line -> 
                line.trim()
                    .removePrefix("-")
                    .removePrefix("*")
                    .removePrefix("•")
                    .trim()
                    .removeSuffix("?")
                    .plus("?")
            }
            .filter { question -> 
                question.isNotBlank() && 
                question.length in 10..150 && 
                question.contains("?") &&
                !question.startsWith("예시") &&
                !question.startsWith("참고")
            }
            .distinct()
            .take(3)
    }

    /**
     * AI 모델 상태 확인
     */
    suspend fun checkModelHealth(): Boolean {
        return try {
            val testRequest = ChatCompletionRequest(
                model = ModelId(openAIConfig.model),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = "테스트"
                    )
                ),
                maxTokens = 10
            )
            
            openAI.chatCompletion(testRequest)
            true
        } catch (e: Exception) {
            log.warn(e) { "Model health check failed" }
            false
        }
    }

    /**
     * 사용 가능한 모델 목록 조회
     */
    suspend fun getAvailableModels(): List<String> {
        return try {
            openAI.models().map { it.id.id }
        } catch (e: Exception) {
            log.warn(e) { "Failed to fetch available models" }
            emptyList()
        }
    }
}

class AIException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)