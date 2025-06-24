package com.challkathon.momento.domain.question.entity

import com.challkathon.momento.domain.question.entity.enums.QuestionCategory
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "question")
class Question(

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var category: QuestionCategory = QuestionCategory.GENERAL,

    @Column(name = "is_ai_generated", nullable = false)
    var isAIGenerated: Boolean = false,

    @Column(name = "generated_date")
    var generatedDate: LocalDate? = null,

    @Column(name = "usage_count")
    var usageCount: Int = 0
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id", nullable = false)
    val id: Long = 0

    fun incrementUsageCount() {
        this.usageCount++
    }
}
