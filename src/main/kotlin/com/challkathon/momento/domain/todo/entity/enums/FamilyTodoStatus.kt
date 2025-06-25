package com.challkathon.momento.domain.todo.entity.enums

enum class FamilyTodoStatus(
    val description: String
) {
    ASSIGNED("할당됨"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료됨"),
    EXPIRED("만료됨")
}