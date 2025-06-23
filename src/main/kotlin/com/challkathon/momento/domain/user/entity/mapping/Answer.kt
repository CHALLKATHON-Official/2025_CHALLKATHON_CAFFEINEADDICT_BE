package com.challkathon.momento.domain.user.entity.mapping

import com.challkathon.momento.domain.user.entity.User
import jakarta.persistence.*

@Entity
@Table(name = "answer")
class Answer(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_question_id", nullable = false)
    val familyQuestion: FamilyQuestion,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
}
