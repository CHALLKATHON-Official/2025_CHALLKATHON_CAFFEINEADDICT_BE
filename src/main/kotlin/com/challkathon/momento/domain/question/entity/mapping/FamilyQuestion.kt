package com.challkathon.momento.domain.question.entity.mapping

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.question.entity.Answer
import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.question.entity.enums.FamilyQuestionStatus
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "family_question")
class FamilyQuestion(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    val family: Family,

    @Column(name = "assigned_at", nullable = false)
    val assignedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "due_date")
    var dueDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: FamilyQuestionStatus = FamilyQuestionStatus.ASSIGNED
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "family_question_id", nullable = false)
    val id: Long = 0

    @OneToMany(mappedBy = "familyQuestion", cascade = [CascadeType.ALL], orphanRemoval = true)
    val answers: MutableList<Answer> = mutableListOf()

    fun isOverdue(): Boolean {
        return dueDate?.let { it < LocalDateTime.now() } ?: false
    }

    fun getAnswerRate(): Double {
        if (family.count == 0) return 0.0
        return (answers.size.toDouble() / family.count) * 100
    }

    fun updateStatus() {
        status = when {
            isOverdue() -> FamilyQuestionStatus.EXPIRED
            answers.size >= family.count -> FamilyQuestionStatus.COMPLETED
            answers.isNotEmpty() -> FamilyQuestionStatus.IN_PROGRESS
            else -> FamilyQuestionStatus.ASSIGNED
        }
    }
}
