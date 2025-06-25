package com.challkathon.momento.domain.question.entity.mapping

import com.challkathon.momento.domain.question.entity.Question
import com.challkathon.momento.domain.user.entity.User
import com.challkathon.momento.global.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "user_question",
    indexes = [
        Index(name = "idx_user_question_user", columnList = "user_id"),
        Index(name = "idx_user_question_created", columnList = "created_at")
    ]
)
class UserQuestion(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,
    
    @Column(name = "ai_model", length = 50)
    val aiModel: String? = null,
    
    @Column(name = "is_answered", nullable = false)
    var isAnswered: Boolean = false
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_question_id", nullable = false)
    val id: Long = 0
    
    fun markAsAnswered() {
        this.isAnswered = true
    }
}
