package com.challkathon.momento.config

import com.challkathon.momento.domain.question.ai.AssistantService
import com.challkathon.momento.domain.question.ai.FamilyContextAnalyzer
import com.challkathon.momento.domain.question.ai.QuestionGenerationManager
import com.challkathon.momento.domain.question.service.FamilyQuestionService
import org.mockito.Mockito
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestConfiguration {

    @Bean
    @Primary
    fun mockAssistantService(): AssistantService {
        return Mockito.mock(AssistantService::class.java)
    }

    @Bean
    @Primary
    fun mockFamilyContextAnalyzer(): FamilyContextAnalyzer {
        return Mockito.mock(FamilyContextAnalyzer::class.java)
    }

    @Bean
    @Primary
    fun mockFamilyQuestionService(): FamilyQuestionService {
        return Mockito.mock(FamilyQuestionService::class.java)
    }
}