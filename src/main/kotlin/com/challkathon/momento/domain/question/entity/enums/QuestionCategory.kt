package com.challkathon.momento.domain.question.entity.enums

enum class QuestionCategory(val displayName: String, val korean: String) {
    MEMORY("Memory", "추억"),
    DAILY("Daily", "일상"),
    FUTURE("Future", "미래"),
    GRATITUDE("Gratitude", "감사"),
    GENERAL("General", "일반")
}
