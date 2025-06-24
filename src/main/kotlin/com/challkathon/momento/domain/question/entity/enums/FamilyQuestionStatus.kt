package com.challkathon.momento.domain.question.entity.enums

enum class FamilyQuestionStatus(val displayName: String) {
    ASSIGNED("할당됨"),
    IN_PROGRESS("진행중"),
    COMPLETED("완료됨"),
    EXPIRED("만료됨")
}
