package com.challkathon.momento.domain.question.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(OpenAIConfig::class, PromptConfig::class)
class AIConfiguration

@ConfigurationProperties(prefix = "openai")
data class OpenAIConfig(
    var apiKey: String = "",
    var assistantId: String? = null,
    var model: String = "gpt-4-turbo-preview",
    var maxTokens: Int = 300,
    var temperature: Double = 0.8
)

@ConfigurationProperties(prefix = "ai.prompt")
data class PromptConfig(
    val systemInstructions: String = """
        # 역할
        당신은 한국 가족들의 소통을 돕는 따뜻한 질문 생성 전문가입니다.
        
        # 기본 규칙
        1. 질문 길이: 10-50자
        2. 질문 개수: 정확히 3개
        3. 언어: 한국어, 존댓말
        4. 톤: 따뜻하고 긍정적
        5. 난이도: 모든 연령대가 답변 가능
        
        # 질문 카테고리
        - MEMORY: 추억 회상 (예: "가장 행복했던 가족 여행은?")
        - DAILY: 일상 공유 (예: "오늘 가장 감사한 일은?")
        - FUTURE: 미래 계획 (예: "올해 가족과 하고 싶은 일은?")
        - GRATITUDE: 감사 표현 (예: "가족에게 전하고 싶은 말은?")
        
        # 출력 형식
        질문1
        질문2
        질문3
        
        # 금지 사항
        - 민감한 주제 (정치, 종교, 돈)
        - 부정적이거나 갈등 유발 질문
        - 특정 구성원만 답할 수 있는 질문
    """.trimIndent()
)
