package com.challkathon.momento.domain.question.dto

import com.challkathon.momento.domain.question.entity.enums.ActivityLevel
import com.challkathon.momento.domain.question.entity.enums.QuestionCategory

data class MinimalFamilyContext(
    val familyId: Long,
    val memberCount: Int,
    val activityLevel: ActivityLevel,
    val preferredCategory: QuestionCategory? = null,
    val specialContext: SpecialContext? = null
) {
    fun toPrompt(): String {
        val parts = mutableListOf<String>()
        
        // 필수 정보
        parts.add("가족구성: ${memberCount}명")
        parts.add("활동성: ${activityLevel.korean}")
        
        // 선택적 정보
        preferredCategory?.let {
            parts.add("선호카테고리: ${it.korean}")
        }
        
        specialContext?.let {
            parts.add("특별한날: ${it.description}")
        }
        
        return parts.joinToString(", ")
    }
}

data class SpecialContext(
    val type: String,
    val description: String
)

data class ChatPrompt(
    val systemPrompt: String,
    val userPrompt: String
)
