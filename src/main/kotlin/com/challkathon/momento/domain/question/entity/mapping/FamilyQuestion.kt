package com.challkathon.momento.domain.question.entity.mapping

import com.challkathon.momento.domain.family.entity.Family
import com.challkathon.momento.domain.question.entity.Question
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
    @Column(name = "family_question_id", nullable = false)
    val id: Long = 0
}
