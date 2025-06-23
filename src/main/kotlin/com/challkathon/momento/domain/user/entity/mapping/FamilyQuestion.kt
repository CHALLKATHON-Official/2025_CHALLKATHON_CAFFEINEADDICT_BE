package com.challkathon.momento.domain.user.entity.mapping

import com.challkathon.momento.domain.user.entity.Family
import com.challkathon.momento.domain.user.entity.Question
import jakarta.persistence.*

@Entity
@Table(name = "family_question")
class FamilyQuestion(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    val question: Question,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    val family: Family
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
